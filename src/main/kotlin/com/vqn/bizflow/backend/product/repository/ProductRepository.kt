package com.vqn.bizflow.backend.product.repository

import com.vqn.bizflow.backend.product.entity.ProductEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.util.UUID

/**
 * Repository cho ProductEntity.
 *
 * @SQLRestriction(is_active = 1) tự động thêm vào mọi query →
 * không cần filter thủ công isActive.
 */
interface ProductRepository : JpaRepository<ProductEntity, UUID> {

    /** Kiểm tra trùng tên (chỉ active — nhờ @SQLRestriction) */
    fun existsByNameAndOwnerId(name: String, ownerId: UUID): Boolean

    /** Kiểm tra trùng barcode (chỉ active — nhờ @SQLRestriction) */
    fun existsByBarcode(barcode: String): Boolean

    /** Tìm SP của 1 owner (có JOIN FETCH category + primaryUnit tránh N+1) */
    @EntityGraph(attributePaths = ["category", "primaryUnit"])
    @Query("SELECT p FROM ProductEntity p WHERE p.ownerId = :ownerId")
    fun findByOwnerId(@Param("ownerId") ownerId: UUID, pageable: Pageable): Page<ProductEntity>

    /** Tìm kiếm SP theo tên (LIKE) + lọc categoryId */
    @EntityGraph(attributePaths = ["category", "primaryUnit"])
    @Query("""
        SELECT p FROM ProductEntity p 
        WHERE p.ownerId = :ownerId 
        AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
    """)
    fun searchByOwnerId(
        @Param("ownerId") ownerId: UUID,
        @Param("search") search: String?,
        @Param("categoryId") categoryId: UUID?,
        pageable: Pageable,
    ): Page<ProductEntity>

    /** Danh sách SP sắp hết hàng (tồn <= ngưỡng) — chỉ active nhờ @SQLRestriction */
    fun findByOwnerIdAndStockLessThanEqualOrderByStockAsc(
        ownerId: UUID, threshold: BigDecimal,
    ): List<ProductEntity>

    /** Atomic increment stock — dùng cho stock import */
    @Modifying
    @Query("UPDATE ProductEntity p SET p.stock = p.stock + :quantity WHERE p.id = :productId")
    fun incrementStock(
        @Param("productId") productId: UUID,
        @Param("quantity") quantity: BigDecimal,
    )
}
