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
import com.vqn.bizflow.backend.product.repository.ProductImageRepository
import com.vqn.bizflow.backend.product.repository.ProductRepository
import com.vqn.bizflow.backend.product.repository.ProductUnitRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.orm.ObjectOptimisticLockingFailureException
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
 * - Optimistic locking (race condition)
 *
 * Dùng MapStruct (ProductMapper) để chuyển Entity → Response tự động.
 * @Formula fields (categoryName, primaryUnitName) tính qua SQL subquery
 * tại query time — không cần @ManyToOne read-only.
 */
@Service
@Transactional
class ProductService(
    private val productRepo: ProductRepository,
    private val productUnitRepo: ProductUnitRepository,
    private val productImageRepo: ProductImageRepository,
    private val productMapper: ProductMapper,
) {
    companion object {
        private const val MAX_PAGE_SIZE = 100
        private val VALID_SORT_FIELDS = setOf("name", "price", "stock", "createdAt")
    }

    private val log = LoggerFactory.getLogger(ProductService::class.java)

    // ===== CRUD =====

    /**
     * Tạo sản phẩm mới.
     * UC-01 đến UC-11: validation, duplicate, warning cost>price, warning low stock.
     */
    fun create(userId: UUID, request: CreateProductRequest): ProductResponse {
        val trimmedName = request.name.trim()

        // UC-03: Trùng tên (cùng owner)
        if (productRepo.existsByNameAndOwnerIdAndIsActive(trimmedName, userId, true)) {
            throw DuplicateException("Product with this name already exists")
        }

        // UC-15: Trùng barcode
        if (!request.barcode.isNullOrBlank()) {
            if (productRepo.existsByBarcodeAndIsActive(request.barcode.trim(), true)) {
                throw DuplicateException("Barcode already exists")
            }
        }

        // UC-07: Giá vốn > giá bán (chỉ warning, không block)
        if (request.costPrice != null && request.price < request.costPrice) {
            log.warn("Cost price {} exceeds selling price {} for product '{}'",
                request.costPrice, request.price, trimmedName)
        }

        val entity = request.toEntity(userId)

        // Lưu trước để có id, sau đó mới thêm images (cần productId FK)
        val saved = productRepo.save(entity)

        // Thêm images nếu có
        request.imageKeys.take(ProductEntity.MAX_IMAGES).forEach { key ->
            saved.addImage(objectKey = key, uploadedBy = userId)
        }
        val finalSaved = if (request.imageKeys.isNotEmpty()) productRepo.save(saved) else saved

        // UC-09: Cảnh báo tồn kho thấp ngay khi tạo
        if (finalSaved.stock < finalSaved.minStock) {
            log.warn("Stock {} is below minimum threshold {} for product '{}'",
                finalSaved.stock, finalSaved.minStock, finalSaved.name)
        }

        return productMapper.toResponse(finalSaved)
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
            throw BadRequestException("At least one field must be provided")
        }

        // UC-19/20: Check duplicate name nếu đổi tên
        request.name?.trim()?.let { newName ->
            if (newName != product.name) {
                if (productRepo.existsByNameAndOwnerIdAndIsActive(newName, userId, true)) {
                    throw DuplicateException("Product with this name already exists")
                }
                product.name = newName
            }
        }

        request.categoryId?.let { product.categoryId = it }
        request.primaryUnitId?.let { product.primaryUnitId = it }
        request.price?.let { validatePositive(it, "Price"); product.price = it }
        request.costPrice?.let { product.costPrice = it }
        request.stock?.let { validateNonNegative(it, "Stock"); product.stock = it }
        request.minStock?.let { validateNonNegative(it, "Min stock"); product.minStock = it }
        request.imageUrl?.trim()?.let { product.imageUrl = it }

        // Replace toàn bộ ảnh (nếu imageKeys được gửi)
        request.imageKeys?.let { keys ->
            product.replaceImages(objectKeys = keys, uploadedBy = userId)
        }

        request.barcode?.trim()?.let {
            if (it != product.barcode) {
                if (productRepo.existsByBarcodeAndIsActive(it, true)) {
                    throw DuplicateException("Barcode already exists")
                }
            }
            product.barcode = it
        }
        product.updatedAt = Instant.now()

        try {
            val saved = productRepo.save(product)
            return productMapper.toResponse(saved)
        } catch (e: ObjectOptimisticLockingFailureException) {
            // UC-23: Race condition
            throw ConflictException("Product was modified by another user. Please refresh and retry.")
        }
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
     * Lấy chi tiết sản phẩm.
     */
    @Transactional(readOnly = true)
    fun getById(productId: UUID): ProductResponse {
        val product = productRepo.findById(productId)
            .orElseThrow { ResourceNotFoundException("Product not found") }
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
            productRepo.findByOwnerIdAndIsActive(userId, true, pageable)
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
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
        )
    }

    // ===== Đơn vị tính phụ (ProductUnit) =====

    /** Thêm đơn vị tính phụ cho sản phẩm. */
    fun addUnit(productId: UUID, unitId: UUID, price: BigDecimal, conversionRate: BigDecimal?): ProductUnitEntity {
        if (!productRepo.existsById(productId)) {
            throw ResourceNotFoundException("Product not found")
        }
        if (productUnitRepo.existsByProductIdAndUnitId(productId, unitId)) {
            throw DuplicateException("Unit already exists for this product")
        }
        if (conversionRate != null && conversionRate <= BigDecimal.ZERO) {
            throw BadRequestException("Conversion rate must be greater than 0")
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
            throw BadRequestException("Unit does not belong to this product")
        }
        if (productUnitRepo.countByProductId(productId) <= 1) {
            throw BadRequestException("Product must have at least one unit")
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

    private fun validatePositive(value: BigDecimal, field: String) {
        if (value <= BigDecimal.ZERO) throw BadRequestException("$field must be greater than 0")
    }

    private fun validateNonNegative(value: BigDecimal, field: String) {
        if (value < BigDecimal.ZERO) throw BadRequestException("$field cannot be negative")
    }
}
