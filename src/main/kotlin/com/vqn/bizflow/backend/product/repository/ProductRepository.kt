package com.vqn.bizflow.backend.product.repository

import com.vqn.bizflow.backend.product.entity.ProductEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

/**
 * Repository cho ProductEntity.
 *
 * Các query method tự động filter is_active = true
 * (soft delete — không xóa vật lý, chỉ ẩn).
 */
interface ProductRepository : JpaRepository<ProductEntity, UUID> {

    /** Kiểm tra trùng tên — chỉ check SP đang active */
    fun existsByNameAndOwnerIdAndIsActive(name: String, ownerId: UUID, isActive: Boolean): Boolean

    /** Kiểm tra trùng barcode — chỉ check SP đang active */
    fun existsByBarcodeAndIsActive(barcode: String, isActive: Boolean): Boolean

    /** Tìm SP đang active của 1 owner */
    @Query("SELECT p FROM ProductEntity p WHERE p.ownerId = :ownerId AND p.isActive = :isActive")
    fun findByOwnerIdAndIsActive(@Param("ownerId") ownerId: UUID, @Param("isActive") isActive: Boolean, pageable: Pageable): Page<ProductEntity>

    /** Tìm kiếm SP theo tên (LIKE) trong owner */
    @Query("""
        SELECT p FROM ProductEntity p 
        WHERE p.ownerId = :ownerId 
        AND p.isActive = true
        AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:category IS NULL OR p.category = :category)
    """)
    fun searchByOwnerId(
        @Param("ownerId") ownerId: UUID,
        @Param("search") search: String?,
        @Param("category") category: String?,
        pageable: Pageable,
    ): Page<ProductEntity>

    /** Danh sách SP sắp hết hàng (tồn <= ngưỡng) */
    fun findByOwnerIdAndIsActiveAndStockLessThanEqualOrderByStockAsc(
        ownerId: UUID, isActive: Boolean, threshold: java.math.BigDecimal,
    ): List<ProductEntity>
}
