package com.vqn.bizflow.backend.order.repository

import com.vqn.bizflow.backend.order.entity.OrderItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

/**
 * Repository cho OrderItemEntity.
 *
 * Luôn query theo orderId (không query độc lập).
 */
interface OrderItemRepository : JpaRepository<OrderItemEntity, UUID> {

    /** Lấy tất cả items của 1 đơn hàng */
    fun findByOrderIdOrderByCreatedAt(orderId: UUID): List<OrderItemEntity>

    /** Đếm số items — dùng cho summary */
    fun countByOrderId(orderId: UUID): Int

    /**
     * Batch đếm items cho nhiều orders — tránh N+1 query khi build summary list.
     * Returns: List<[orderId, count]>
     */
    @Query("SELECT oi.orderId, COUNT(oi) FROM OrderItemEntity oi WHERE oi.orderId IN :orderIds GROUP BY oi.orderId")
    fun countByOrderIds(@Param("orderIds") orderIds: List<UUID>): List<Array<Any>>

    // ═══════════════════════════════════════════════
    // Report queries
    // ═══════════════════════════════════════════════

    /** Top sản phẩm bán chạy (từ đơn CONFIRMED) */
    @Query("""
        SELECT i.productId, i.productName, SUM(i.quantity), SUM(i.subtotal)
        FROM OrderItemEntity i 
        WHERE i.orderId IN (
            SELECT o.id FROM OrderEntity o 
            WHERE o.ownerId = :ownerId AND o.status = 'CONFIRMED'
        )
        GROUP BY i.productId, i.productName
        ORDER BY SUM(i.quantity) DESC
    """)
    fun findTopSellingByOwnerId(@Param("ownerId") ownerId: UUID): List<Array<Any>>
}
