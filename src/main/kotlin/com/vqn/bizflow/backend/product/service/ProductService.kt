package com.vqn.bizflow.backend.product.service

import com.vqn.bizflow.backend.dto.PaginationResponse
import com.vqn.bizflow.backend.exception.BadRequestException
import com.vqn.bizflow.backend.exception.ConflictException
import com.vqn.bizflow.backend.exception.DuplicateException
import com.vqn.bizflow.backend.exception.ForbiddenException
import com.vqn.bizflow.backend.exception.ResourceNotFoundException
import com.vqn.bizflow.backend.product.dto.CreateProductRequest
import com.vqn.bizflow.backend.product.dto.ProductResponse
import com.vqn.bizflow.backend.product.dto.UpdateProductRequest
import com.vqn.bizflow.backend.product.dto.toEntity
import com.vqn.bizflow.backend.product.entity.ProductEntity
import com.vqn.bizflow.backend.product.entity.ProductImageEntity
import com.vqn.bizflow.backend.product.entity.ProductUnitEntity
import com.vqn.bizflow.backend.product.mapper.ProductMapper
import com.vqn.bizflow.backend.product.repository.CategoryRepository
import com.vqn.bizflow.backend.product.repository.ProductImageRepository
import com.vqn.bizflow.backend.product.repository.ProductRepository
import com.vqn.bizflow.backend.product.repository.ProductUnitRepository
import com.vqn.bizflow.backend.product.repository.UnitRepository
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
import java.util.UUID

/**
 * ProductService — Business logic cho quản lý sản phẩm.
 *
 * Xử lý tất cả unhappy cases:
 * - Validation đầu vào (tên trống, giá âm, tồn âm...)
 * - Authorization (chỉ owner mới CRUD)
 * - Duplicate (tên trùng, barcode trùng)
 * - Soft delete (không xóa vật lý)
 * - Optimistic locking (race condition) — xử lý bởi GlobalExceptionHandler
 *
 * Dùng MapStruct (ProductMapper) để chuyển Entity → Response tự động.
 * categoryName/primaryUnitName map từ @ManyToOne lazy join.
 */
@Service
@Transactional
class ProductService(
    private val productRepo: ProductRepository,
    private val productUnitRepo: ProductUnitRepository,
    private val productImageRepo: ProductImageRepository,
    private val productMapper: ProductMapper,
    private val categoryRepo: CategoryRepository,
    private val unitRepo: UnitRepository,
    private val messageSource: MessageSource,
) {
    companion object {
        private const val MAX_PAGE_SIZE = 100
        private val VALID_SORT_FIELDS = setOf("name", "price", "stock", "createdAt")
    }

    private val log = LoggerFactory.getLogger(ProductService::class.java)

    /** Helper — lấy i18n message từ MessageSource */
    private fun msg(code: String, vararg args: Any): String =
        messageSource.getMessage(code, args, LocaleContextHolder.getLocale())

    // ===== CRUD =====

    /**
     * Tạo sản phẩm mới.
     * UC-01 đến UC-11: validation, duplicate, warning cost>price, warning low stock.
     */
    fun create(userId: UUID, request: CreateProductRequest): ProductResponse {
        val trimmedName = request.name.trim()

        // UC-03: Trùng tên (cùng owner)
        if (productRepo.existsByNameAndOwnerId(trimmedName, userId)) {
            throw DuplicateException(msg("product.duplicate.name"))
        }

        // UC-15: Trùng barcode
        if (!request.barcode.isNullOrBlank()) {
            if (productRepo.existsByBarcode(request.barcode.trim())) {
                throw DuplicateException(msg("product.duplicate.barcode"))
            }
        }

        // UC-07: Giá vốn > giá bán (chỉ warning, không block)
        if (request.costPrice != null && request.price < request.costPrice) {
            log.warn("Cost price {} exceeds selling price {} for product '{}'",
                request.costPrice, request.price, trimmedName)
        }

        val entity = request.toEntity(userId)

        // Set FK associations via lightweight proxy (không SELECT entity)
        if (request.categoryId != null) {
            entity.category = categoryRepo.getReferenceById(request.categoryId)
        }
        entity.primaryUnit = unitRepo.getReferenceById(request.primaryUnitId)

        // Lưu entity (category/primaryUnit map từ @ManyToOne, không cần refresh)
        val saved = productRepo.save(entity)

        // Persist images + đồng bộ entity.images cho response mapper
        persistImages(product = saved, objectKeys = request.imageKeys, uploadedBy = userId)

        // UC-09: Cảnh báo tồn kho thấp ngay khi tạo
        if (saved.stock < saved.minStock) {
            log.warn("Stock {} is below minimum threshold {} for product '{}'",
                saved.stock, saved.minStock, saved.name)
        }

        return productMapper.toResponse(saved)
    }

    /**
     * Cập nhật sản phẩm.
     * UC-16 đến UC-26: check tồn tại, quyền sở hữu, soft delete, duplicate, optimistic lock.
     */
    fun update(userId: UUID, productId: UUID, request: UpdateProductRequest): ProductResponse {
        val product = findActiveProduct(userId, productId)

        // UC-25: Body rỗng
        if (request.name == null && request.categoryId == null && request.primaryUnitId == null &&
            request.price == null && request.costPrice == null && request.stock == null &&
            request.minStock == null && request.imageUrl == null && request.imageKeys == null &&
            request.barcode == null
        ) {
            throw BadRequestException(msg("product.update.empty"))
        }

        // UC-19/20: Check duplicate name nếu đổi tên
        request.name?.trim()?.let { newName ->
            if (newName != product.name) {
                if (productRepo.existsByNameAndOwnerId(newName, userId)) {
                    throw DuplicateException(msg("product.duplicate.name"))
                }
                product.name = newName
            }
        }

        request.categoryId?.let {
            product.category = categoryRepo.getReferenceById(it)
        }
        request.primaryUnitId?.let {
            product.primaryUnit = unitRepo.getReferenceById(it)
        }
        request.price?.let { validatePositive(it, "Price"); product.price = it }
        request.costPrice?.let { product.costPrice = it }
        request.stock?.let { validateNonNegative(it, "Stock"); product.stock = it }
        request.minStock?.let { validateNonNegative(it, "Min stock"); product.minStock = it }
        request.imageUrl?.trim()?.let { product.imageUrl = it }

        // Persist images + đồng bộ entity.images cho response mapper
        request.imageKeys?.let { keys ->
            persistImages(product = product, objectKeys = keys, uploadedBy = userId)
        }

        request.barcode?.trim()?.let {
            if (it != product.barcode) {
                if (productRepo.existsByBarcode(it)) {
                    throw DuplicateException(msg("product.duplicate.barcode"))
                }
            }
            product.barcode = it
        }
        product.updatedAt = Instant.now()

        // OptimisticLockException được global handler bắt → 409 tự động
        val saved = productRepo.save(product)
        return productMapper.toResponse(saved)
    }

    /**
     * Soft delete — ẩn sản phẩm.
     */
    fun deactivate(userId: UUID, productId: UUID) {
        val product = findActiveProduct(userId, productId)
        if (!product.isActive) {
            throw ConflictException("Product is already deactivated")
        }
        product.isActive = false
        product.updatedAt = Instant.now()
        productRepo.save(product)
    }

    /**
     * Lấy chi tiết sản phẩm (có kiểm tra ownerId — multi-tenant).
     */
    @Transactional(readOnly = true)
    fun getById(userId: UUID, productId: UUID): ProductResponse {
        val product = productRepo.findById(productId)
            .orElseThrow { ResourceNotFoundException("Product not found") }
        if (product.ownerId != userId) {
            throw ResourceNotFoundException(msg("product.not-found"))
        }
        return productMapper.toResponse(product)
    }

    /**
     * Danh sách sản phẩm (phân trang + tìm kiếm + lọc theo categoryId).
     */
    @Transactional(readOnly = true)
    fun list(
        userId: UUID,
        search: String?,
        categoryId: UUID?,
        page: Int,
        size: Int,
        sortBy: String?,
        sortDir: String?,
    ): PaginationResponse<ProductResponse> {
        val actualSortBy = sortBy?.takeIf { it in VALID_SORT_FIELDS } ?: "createdAt"
        val actualSortDir = if (sortDir.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val actualSize = size.coerceIn(1, MAX_PAGE_SIZE)
        val actualPage = if (page < 1) 0 else page - 1

        val pageable = PageRequest.of(actualPage, actualSize, Sort.by(actualSortDir, actualSortBy))

        val result: Page<ProductEntity> = if (search.isNullOrBlank() && categoryId == null) {
            productRepo.findByOwnerId(userId, pageable)
        } else {
            productRepo.searchByOwnerId(
                ownerId = userId,
                search = search?.takeIf { it.isNotBlank() },
                categoryId = categoryId,
                pageable = pageable,
            )
        }

        return PaginationResponse.of(
            data = result.content.map { productMapper.toResponse(it) },
            // result.number là 0-based (Spring Data) — convert về 1-based cho API
            page = result.number + 1,
            size = result.size,
            totalElements = result.totalElements,
        )
    }

    // ===== Đơn vị tính phụ (ProductUnit) =====

    /** Thêm đơn vị tính phụ cho sản phẩm. */
    fun addUnit(productId: UUID, unitId: UUID, price: BigDecimal, conversionRate: BigDecimal?): ProductUnitEntity {
        if (!productRepo.existsById(productId)) {
            throw ResourceNotFoundException(msg("product.not-found"))
        }
        if (productUnitRepo.existsByProductIdAndUnitId(productId, unitId)) {
            throw DuplicateException(msg("product.unit.duplicate"))
        }
        if (conversionRate != null && conversionRate <= BigDecimal.ZERO) {
            throw BadRequestException(msg("product.unit.invalid-conversion"))
        }
        return productUnitRepo.save(
            ProductUnitEntity(productId = productId, unitId = unitId, price = price, conversionRate = conversionRate)
        )
    }

    /** Xóa đơn vị tính phụ (không xóa được unit cuối cùng). */
    fun removeUnit(productId: UUID, unitId: UUID) {
        val unit = productUnitRepo.findById(unitId)
            .orElseThrow { ResourceNotFoundException("Unit not found") }
        if (unit.productId != productId) {
            throw BadRequestException(msg("product.unit.not-belong"))
        }
        if (productUnitRepo.countByProductId(productId) <= 1) {
            throw BadRequestException(msg("product.unit.min-one"))
        }
        productUnitRepo.delete(unit)
    }

    // ===== Helpers =====

    private fun findActiveProduct(userId: UUID, productId: UUID): ProductEntity {
        val product = productRepo.findById(productId)
            .orElseThrow { ResourceNotFoundException("Product not found") }
        if (product.ownerId != userId) {
            throw ForbiddenException("You can only edit your own products")
        }
        return product
    }

    /**
     * Persist image list cho product: replace toàn bộ → re-sync entity collection.
     *
     * Dùng ProductImageRepository.save()/deleteByProductId() thay vì
     * @OneToMany cascade để đảm bảo Hibernate 7 + Kotlin generate đúng
     * INSERT/DELETE SQL (PROPERTY access + param-property target có thể
     * gây issue với cascade tracking).
     *
     * Sau khi persist, đồng bộ entity.images để ProductMapper.toResponse()
     * đọc được imageKeys mà không cần extra SELECT.
     */
    private fun persistImages(product: ProductEntity, objectKeys: List<String>, uploadedBy: UUID) {
        val productId = requireNotNull(product.id) { "Product ID must not be null when persisting images" }
        // Delete existing + insert new (max MAX_IMAGES)
        productImageRepo.deleteByProductId(productId)
        val keys = objectKeys.take(ProductEntity.MAX_IMAGES)
        keys.forEachIndexed { idx, key ->
            productImageRepo.save(
                ProductImageEntity(
                    product = product,
                    objectKey = key,
                    position = idx,
                    uploadedBy = uploadedBy,
                )
            )
        }

        // Sync entity.images cho mapper
        product.images.clear()
        productImageRepo.findImagesByProductId(productId).forEach { product.images.add(it) }
    }

    private fun validatePositive(value: BigDecimal, field: String) {
        if (value <= BigDecimal.ZERO) throw BadRequestException(msg("product.validation.positive", field))
    }

    private fun validateNonNegative(value: BigDecimal, field: String) {
        if (value < BigDecimal.ZERO) throw BadRequestException(msg("product.validation.non-negative", field))
    }
}
