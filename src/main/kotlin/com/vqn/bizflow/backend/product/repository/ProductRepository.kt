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

    /** Tìm kiếm SP theo tên (LIKE) + lọc categoryId — cùng owner */
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

    /** Toàn bộ SP active (không lọc owner — dùng cho ADMIN). */
    @EntityGraph(attributePaths = ["category", "primaryUnit"])
    @Query("SELECT p FROM ProductEntity p")
    fun findAllActive(pageable: Pageable): Page<ProductEntity>

    /** Tìm kiếm SP toàn hệ thống (không lọc owner — dùng cho ADMIN). */
    @EntityGraph(attributePaths = ["category", "primaryUnit"])
    @Query("""
        SELECT p FROM ProductEntity p 
        WHERE (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
    """)
    fun searchAllActive(
        @Param("search") search: String?,
        @Param("categoryId") categoryId: UUID?,
        pageable: Pageable,
    ): Page<ProductEntity>

    /** Danh sách SP sắp hết hàng (tồn <= ngưỡng) — chỉ active nhờ @SQLRestriction */
    fun findByOwnerIdAndStockLessThanEqualOrderByStockAsc(
        ownerId: UUID, threshold: BigDecimal,
    ): List<ProductEntity>

    /**
     * Atomic increment stock — dùng cho stock import / hoàn kho khi hủy đơn.
     * Returns: số rows affected (0 = product không tồn tại).
     */
    @Modifying
    @Query("UPDATE ProductEntity p SET p.stock = p.stock + :quantity WHERE p.id = :productId")
    fun incrementStock(
        @Param("productId") productId: UUID,
        @Param("quantity") quantity: BigDecimal,
    ): Int

    /**
     * Atomic decrement stock — dùng cho order confirm (trừ kho).
     * Returns: số rows affected (0 = stock không đủ hoặc product không tồn tại).
     * Guard `p.stock >= :quantity` ngăn race condition giữa check và write.
     */
    @Modifying
    @Query("UPDATE ProductEntity p SET p.stock = p.stock - :quantity WHERE p.id = :productId AND p.stock >= :quantity")
    fun decrementStock(
        @Param("productId") productId: UUID,
        @Param("quantity") quantity: BigDecimal,
    ): Int

    // ═══════════════════════════════════════════════
    // Report queries
    // ═══════════════════════════════════════════════

    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.ownerId = :ownerId")
    fun countByOwnerId(@Param("ownerId") ownerId: UUID): Long

    @Query("SELECT COALESCE(SUM(p.stock * COALESCE(p.costPrice, 0)), 0) FROM ProductEntity p WHERE p.ownerId = :ownerId")
    fun sumInventoryValue(@Param("ownerId") ownerId: UUID): BigDecimal

    @Query("SELECT COALESCE(SUM(p.stock), 0) FROM ProductEntity p WHERE p.ownerId = :ownerId")
    fun sumTotalStock(@Param("ownerId") ownerId: UUID): BigDecimal

    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.ownerId = :ownerId AND p.stock <= p.minStock")
    fun countLowStock(@Param("ownerId") ownerId: UUID): Long

    @Query("SELECT COALESCE(p.category.name, '(No category)'), COUNT(p) FROM ProductEntity p WHERE p.ownerId = :ownerId GROUP BY p.category.name ORDER BY COUNT(p) DESC")
    fun countByCategory(@Param("ownerId") ownerId: UUID): List<Array<Any>>

    // ═══════════════════════════════════════════════
    // Platform-wide report queries (no owner filter)
    // ═══════════════════════════════════════════════

    @Query("SELECT COUNT(p) FROM ProductEntity p")
    fun countAllActive(): Long

    @Query("SELECT COALESCE(SUM(p.stock * COALESCE(p.costPrice, 0)), 0) FROM ProductEntity p")
    fun sumInventoryValueAllActive(): BigDecimal

    @Query("SELECT COALESCE(SUM(p.stock), 0) FROM ProductEntity p")
    fun sumTotalStockAllActive(): BigDecimal

    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.stock <= p.minStock")
    fun countLowStockAllActive(): Long

    @Query("SELECT p FROM ProductEntity p WHERE p.stock <= p.minStock ORDER BY p.stock ASC")
    fun findLowStockAllActive(): List<ProductEntity>

    @Query("SELECT COALESCE(p.category.name, '(No category)'), COUNT(p) FROM ProductEntity p GROUP BY p.category.name ORDER BY COUNT(p) DESC")
    fun countByCategoryAllActive(): List<Array<Any>>
}
