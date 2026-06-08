package com.vqn.bizflow.backend.order.repository

import com.vqn.bizflow.backend.order.entity.OrderItemEntity
import org.springframework.data.jpa.repository.JpaRepository
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
}
