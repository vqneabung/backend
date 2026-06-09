package com.vqn.bizflow.backend.order.service

import com.vqn.bizflow.backend.dto.PaginationResponse
import com.vqn.bizflow.backend.exception.BadRequestException
import com.vqn.bizflow.backend.exception.ResourceNotFoundException
import com.vqn.bizflow.backend.order.dto.CreateOrderItemRequest
import com.vqn.bizflow.backend.order.dto.CreateOrderRequest
import com.vqn.bizflow.backend.order.dto.OrderResponse
import com.vqn.bizflow.backend.order.dto.OrderSummaryResponse
import com.vqn.bizflow.backend.order.entity.OrderEntity
import com.vqn.bizflow.backend.order.entity.OrderItemEntity
import com.vqn.bizflow.backend.order.entity.OrderStatus
import com.vqn.bizflow.backend.order.mapper.OrderMapper
import com.vqn.bizflow.backend.order.repository.OrderItemRepository
import com.vqn.bizflow.backend.order.repository.OrderRepository
import com.vqn.bizflow.backend.product.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
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
 * Service quản lý đơn hàng.
 *
 * 1 transaction cho toàn bộ: tạo đơn + insert items + trừ/kho (nếu CONFIRMED).
 * Multi-tenant qua ownerId.
 * Soft delete prevent xóa vật lý.
 * Optimistic locking protect concurrent stock updates.
 */
@Service
@Transactional
class OrderService(
    private val orderRepo: OrderRepository,
    private val orderItemRepo: OrderItemRepository,
    private val productRepo: ProductRepository,
    private val orderMapper: OrderMapper,
    private val messageSource: MessageSource,
) {
    private val log = LoggerFactory.getLogger(OrderService::class.java)

    /** Helper — lấy i18n message từ MessageSource */
    private fun msg(code: String, vararg args: Any): String =
        messageSource.getMessage(code, args, LocaleContextHolder.getLocale())

    companion object {
        private const val MAX_SIZE = 100
    }

    /**
     * Tạo đơn hàng.
     *
     * Nếu status = CONFIRMED → tự động trừ kho từng sản phẩm.
     * Nếu status = DRAFT → lưu nháp (không ảnh hưởng kho).
     */
    fun create(userId: UUID, request: CreateOrderRequest): OrderResponse {
        // 1. Validate status
        val status = request.status.uppercase()
        if (status !in listOf(OrderStatus.DRAFT, OrderStatus.CONFIRMED)) {
            throw BadRequestException(msg("order.status.invalid"))
        }

        // 2. Validate items
        if (request.items.isEmpty()) {
            throw BadRequestException(msg("order.items.empty"))
        }
        validateItems(request.items)

        // 3. Generate reference number
        val refNumber = generateReferenceNumber(userId)

        // 4. Calculate total
        var totalAmount = BigDecimal.ZERO
        val itemData = request.items.map { item ->
            val product = productRepo.findById(item.productId)
                .orElseThrow { ResourceNotFoundException("Product not found: ${item.productId}") }

            val subtotal = item.quantity.multiply(item.unitPrice)
            totalAmount = totalAmount.add(subtotal)

            ProductItemData(
                productId = item.productId,
                productName = product.name,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                subtotal = subtotal,
            )
        }

        // 5. Kiểm tra tồn kho nếu CONFIRMED
        if (status == OrderStatus.CONFIRMED) {
            checkStockAvailability(itemData)
        }

        // 6. Tạo order header
        val paidAmount = if (status == OrderStatus.CONFIRMED) totalAmount else BigDecimal.ZERO
        val entity = OrderEntity(
            ownerId = userId,
            referenceNumber = refNumber,
            customerId = request.customerId,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            debtAmount = totalAmount.subtract(paidAmount),
            status = status,
            notes = request.notes?.trim(),
        )
        val saved = orderRepo.save(entity)
        val savedId = requireNotNull(saved.id) { "Order ID must not be null after save" }

        // 7. Tạo items
        val itemEntities = itemData.map { data ->
            OrderItemEntity(
                orderId = savedId,
                productId = data.productId,
                productName = data.productName,
                quantity = data.quantity,
                unitPrice = data.unitPrice,
                subtotal = data.subtotal,
            )
        }
        val savedItems = orderItemRepo.saveAll(itemEntities)

        // 8. Trừ kho nếu CONFIRMED
        if (status == OrderStatus.CONFIRMED) {
            itemData.forEach { data ->
                val rowsAffected = productRepo.decrementStock(data.productId, data.quantity)
                if (rowsAffected == 0) {
                    // Race: stock đã bị thay đổi giữa checkStockAvailability và decrement
                    // → throw để rollback toàn bộ transaction
                    throw BadRequestException(msg("order.stock-insufficient"))
                }
            }
            log.info("Order {} confirmed: stock deducted for {} items", refNumber, itemData.size)
        }

        log.info("Order {} created: {} items, total={}, status={}", refNumber, itemData.size, totalAmount, status)

        // 9. Build response
        val itemResponses = savedItems.map { orderMapper.toItemResponse(it) }
        return orderMapper.toDetailResponse(saved, itemResponses)
    }

    /**
     * Danh sách đơn hàng (phân trang, filter status/date).
     */
    fun list(
        userId: UUID,
        page: Int,
        size: Int,
        status: String?,
        fromDate: Instant?,
        toDate: Instant?,
    ): PaginationResponse<OrderSummaryResponse> {
        val p = page.coerceAtLeast(1)
        val s = size.coerceIn(1, MAX_SIZE)
        val pageable = PageRequest.of(p - 1, s, Sort.by(Sort.Direction.DESC, "createdAt"))

        val result = orderRepo.findByOwnerId(userId, status?.takeIf { it.isNotBlank() }, fromDate, toDate, pageable)

        // Batch count items tránh N+1 query (1 query thay vì N queries)
        val orderIds = result.content.mapNotNull { it.id }
        val itemCountMap: Map<UUID, Int> = if (orderIds.isEmpty()) {
            emptyMap()
        } else {
            orderItemRepo.countByOrderIds(orderIds)
                .associate { row -> row[0] as UUID to (row[1] as Long).toInt() }
        }

        val summaries = result.content.map { entity ->
            OrderSummaryResponse(
                id = requireNotNull(entity.id) { "Order ID must not be null" },
                customerId = entity.customerId,
                referenceNumber = entity.referenceNumber,
                totalAmount = entity.totalAmount,
                paidAmount = entity.paidAmount,
                debtAmount = entity.debtAmount,
                status = entity.status,
                itemCount = itemCountMap[entity.id] ?: 0,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )
        }

        return PaginationResponse.of(
            data = summaries,
            page = p,
            size = s,
            totalElements = result.totalElements,
        )
    }

    /**
     * Xem chi tiết 1 đơn hàng (kèm items).
     */
    fun getById(userId: UUID, orderId: UUID): OrderResponse {
        val entity = orderRepo.findByIdAndOwnerId(orderId, userId)
            ?: throw ResourceNotFoundException(msg("order.not-found"))

        val items = orderItemRepo.findByOrderIdOrderByCreatedAt(orderId)
        val itemResponses = items.map { orderMapper.toItemResponse(it) }

        return orderMapper.toDetailResponse(entity, itemResponses)
    }

    /**
     * Hủy đơn hàng — chỉ hủy được DRAFT hoặc CONFIRMED.
     * Nếu CONFIRMED → hoàn lại stock cho từng sản phẩm.
     */
    fun cancel(userId: UUID, orderId: UUID, notes: String?): OrderResponse {
        val entity = orderRepo.findByIdAndOwnerId(orderId, userId)
            ?: throw ResourceNotFoundException(msg("order.not-found"))

        if (entity.status == OrderStatus.CANCELLED) {
            throw BadRequestException(msg("order.already-cancelled"))
        }

        val previousStatus = entity.status
        entity.status = OrderStatus.CANCELLED
        entity.notes = notes?.trim() ?: entity.notes
        entity.updatedAt = Instant.now()

        // Hoàn stock nếu trước đó là CONFIRMED (đã trừ kho)
        if (previousStatus == OrderStatus.CONFIRMED) {
            val items = orderItemRepo.findByOrderIdOrderByCreatedAt(orderId)
            items.forEach { item ->
                val rowsAffected = productRepo.incrementStock(item.productId, item.quantity)
                if (rowsAffected == 0) {
                    // Product đã bị xóa/soft-delete trong lúc đơn CONFIRMED
                    // → log warning, không fail cancellation
                    log.warn(
                        "Cannot restore stock for product {} when cancelling order {}: product not found",
                        item.productId, entity.referenceNumber
                    )
                }
            }
            log.info("Order {} cancelled: stock restored for {} items", entity.referenceNumber, items.size)
        }

        val saved = orderRepo.save(entity)

        // Build response with items
        val items = orderItemRepo.findByOrderIdOrderByCreatedAt(orderId)
        val itemResponses = items.map { orderMapper.toItemResponse(it) }
        return orderMapper.toDetailResponse(saved, itemResponses)
    }

    // ── Private helpers ────────────────────────────────────

    /** Validate items: check quantity > 0, unitPrice > 0 */
    private fun validateItems(items: List<CreateOrderItemRequest>) {
        items.forEach { item ->
            if (item.quantity <= BigDecimal.ZERO) {
                throw BadRequestException(msg("order.items.quantity-positive"))
            }
            if (item.unitPrice <= BigDecimal.ZERO) {
                throw BadRequestException(msg("order.items.price-positive"))
            }
        }
    }

    /** Kiểm tra tồn kho đủ cho tất cả items */
    private fun checkStockAvailability(items: List<ProductItemData>) {
        items.forEach { data ->
            val product = productRepo.findById(data.productId)
                .orElseThrow { ResourceNotFoundException(msg("product.not-found")) }
            if (product.stock < data.quantity) {
                throw BadRequestException(msg("order.stock-insufficient"))
            }
        }
    }

    /** Tự sinh reference number: DH-YYYYMMDD-XXX */
    private fun generateReferenceNumber(ownerId: UUID): String {
        val today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))
        val dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val startOfDay = today.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant()
        val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant()
        val count = orderRepo.countByOwnerIdAndCreatedAtBetween(ownerId, startOfDay, endOfDay)
        return "DH-$dateStr-${(count + 1).toString().padStart(3, '0')}"
    }

    /** Internal data class cho item processing */
    private data class ProductItemData(
        val productId: UUID,
        val productName: String,
        val quantity: BigDecimal,
        val unitPrice: BigDecimal,
        val subtotal: BigDecimal,
    )
}
