package com.vqn.bizflow.backend.inventory.service

import com.vqn.bizflow.backend.dto.PaginationResponse
import com.vqn.bizflow.backend.exception.BadRequestException
import com.vqn.bizflow.backend.exception.ResourceNotFoundException
import com.vqn.bizflow.backend.inventory.dto.CreateStockImportItemRequest
import com.vqn.bizflow.backend.inventory.dto.CreateStockImportRequest
import com.vqn.bizflow.backend.inventory.dto.StockImportResponse
import com.vqn.bizflow.backend.inventory.dto.StockImportSummaryResponse
import com.vqn.bizflow.backend.inventory.entity.StockImportEntity
import com.vqn.bizflow.backend.inventory.entity.StockImportItemEntity
import com.vqn.bizflow.backend.inventory.mapper.StockImportMapper
import com.vqn.bizflow.backend.inventory.repository.StockImportItemRepository
import com.vqn.bizflow.backend.inventory.repository.StockImportRepository
import com.vqn.bizflow.backend.product.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Service quản lý nhập kho.
 *
 * 1 transaction cho toàn bộ: tạo phiếu nhập + update stock + insert items.
 * Multi-tenant qua ownerId.
 * Soft disable prevent xóa vật lý.
 * Optimistic locking protect concurrent stock updates.
 */
@Service
@Transactional
class StockImportService(
    private val stockImportRepo: StockImportRepository,
    private val stockImportItemRepo: StockImportItemRepository,
    private val productRepo: ProductRepository,
    private val stockImportMapper: StockImportMapper,
    private val messageSource: MessageSource,
) {
    private val log = LoggerFactory.getLogger(StockImportService::class.java)

    /** Helper — lấy i18n message từ MessageSource */
    private fun msg(code: String, vararg args: Any): String =
        messageSource.getMessage(code, args, LocaleContextHolder.getLocale())

    companion object {
        private const val DEFAULT_PAGE = 1
        private const val DEFAULT_SIZE = 20
        private const val MAX_SIZE = 100
    }

    /**
     * Tạo phiếu nhập kho + tự động tăng tồn kho cho từng sản phẩm.
     */
    fun create(userId: UUID, request: CreateStockImportRequest): StockImportResponse {
        // 1. Validate items
        validateItems(request.items)

        // 2. Generate reference number nếu không có
        val refNumber = request.referenceNumber?.trim()?.ifBlank { null }
            ?: generateReferenceNumber(userId)

        // 3. Tạo entity
        val entity = StockImportEntity(
            ownerId = userId,
            referenceNumber = refNumber,
            supplier = request.supplier?.trim(),
            notes = request.notes?.trim(),
            importDate = request.importDate ?: Instant.now(),
        )
        val saved = stockImportRepo.save(entity)
        val savedId = requireNotNull(saved.id) { "StockImport ID must not be null after save" }

        // 4. Tạo items + update stock + tính tổng
        var totalCost = BigDecimal.ZERO
        val itemEntities = request.items.map { item ->
            // Kiểm tra product tồn tại
            if (!productRepo.existsById(item.productId)) {
                throw ResourceNotFoundException(msg("stock-import.product-not-found"))
            }

            val subtotal = item.quantity.multiply(item.unitCost)
            totalCost = totalCost.add(subtotal)

            StockImportItemEntity(
                stockImportId = savedId,
                productId = item.productId,
                quantity = item.quantity,
                unitCost = item.unitCost,
                subtotal = subtotal,
            )
        }

        // Batch save items
        val savedItems = stockImportItemRepo.saveAll(itemEntities)

        // 5. Update product stock (atomic increment)
        request.items.forEach { item ->
            val rowsAffected = productRepo.incrementStock(item.productId, item.quantity)
            if (rowsAffected == 0) {
                // Product bị xóa/soft-delete giữa existsById check và increment (edge case race)
                // → log warning nhưng KHÔNG fail (đã validate trước đó)
                log.warn(
                    "Cannot increment stock for product {} (import ref={}): product not found",
                    item.productId, refNumber
                )
            }
        }

        // 6. Update totalCost trên entity
        saved.totalCost = totalCost
        val finalSaved = stockImportRepo.save(saved)

        log.info("Stock import {} created: {} items, total={}", refNumber, itemEntities.size, totalCost)

        // 7. Build response
        val itemResponses = savedItems.map { item ->
            val productName = productRepo.findById(item.productId)
                .map { it.name }
                .orElse("(deleted)")
            stockImportMapper.toItemResponse(item, productName)
        }

        return stockImportMapper.toDetailResponse(finalSaved, itemResponses)
    }

    /**
     * List phiếu nhập (phân trang, mới nhất trước).
     */
    fun list(userId: UUID, page: Int, size: Int): PaginationResponse<StockImportSummaryResponse> {
        val p = page.coerceAtLeast(1)
        val s = size.coerceIn(1, MAX_SIZE)
        val pageable = PageRequest.of(p - 1, s, Sort.by(Sort.Direction.DESC, "createdAt"))

        val result: Page<StockImportEntity> =
            stockImportRepo.findByOwnerIdOrderByCreatedAtDesc(userId, pageable)

        // Batch count items tránh N+1 query (1 query thay vì N queries)
        val stockImportIds = result.content.mapNotNull { it.id }
        val itemCountMap: Map<UUID, Int> = if (stockImportIds.isEmpty()) {
            emptyMap()
        } else {
            stockImportItemRepo.countByStockImportIds(stockImportIds)
                .associate { row -> row[0] as UUID to (row[1] as Long).toInt() }
        }

        val summaries = result.content.map { entity ->
            val itemCount = itemCountMap[entity.id] ?: 0
            StockImportSummaryResponse.from(entity, itemCount)
        }

        return PaginationResponse.of(
            data = summaries,
            page = p,
            size = s,
            totalElements = result.totalElements,
        )
    }

    /**
     * Xem chi tiết 1 phiếu nhập (kèm items).
     */
    fun getById(userId: UUID, importId: UUID): StockImportResponse {
        val entity = stockImportRepo.findByIdAndOwnerId(importId, userId)
            ?: throw ResourceNotFoundException(msg("stock-import.not-found"))

        val items = stockImportItemRepo.findByStockImportIdOrderByCreatedAt(importId)
        val itemResponses = items.map { item ->
            val productName = productRepo.findById(item.productId)
                .map { it.name }
                .orElse("(deleted)")
            stockImportMapper.toItemResponse(item, productName)
        }

        return stockImportMapper.toDetailResponse(entity, itemResponses)
    }

    // ── Private helpers ────────────────────────────────────

    /** Validate items: check quantity, unitCost > 0 */
    private fun validateItems(items: List<CreateStockImportItemRequest>) {
        items.forEach { item ->
            if (item.quantity <= BigDecimal.ZERO) {
                throw BadRequestException(msg("stock-import.quantity-positive"))
            }
            if (item.unitCost <= BigDecimal.ZERO) {
                throw BadRequestException(msg("stock-import.cost-positive"))
            }
        }
    }

    /** Tự sinh reference number: NK-YYYYMMDD-XXX */
    private fun generateReferenceNumber(ownerId: UUID): String {
        val today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))
        val dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val startOfDay = today.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant()
        val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant()
        val count = stockImportRepo.countByOwnerIdAndCreatedAtBetween(ownerId, startOfDay, endOfDay)
        return "NK-$dateStr-${(count + 1).toString().padStart(3, '0')}"
    }
}
