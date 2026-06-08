package com.vqn.bizflow.backend.order.entity

import com.vqn.bizflow.backend.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Đơn hàng bán tại quầy (at-counter order).
 *
 * Khi tạo với status = CONFIRMED, hệ thống tự động trừ kho
 * từng sản phẩm (FR-14). Khi hủy (CANCELLED), hoàn lại stock.
 *
 * Multi-tenant qua ownerId.
 * Soft delete: is_active = false (giữ audit trail).
 * Optimistic locking: @Version.
 *
 * Status workflow:
 *   DRAFT → CONFIRMED (trừ kho)
 *   DRAFT → CANCELLED
 *   CONFIRMED → CANCELLED (hoàn kho)
 */
@Entity
@Table(
    name = "orders",
    indexes = [
        Index(name = "idx_orders_owner_id", columnList = "owner_id"),
        Index(name = "idx_orders_owner_status", columnList = "owner_id, status"),
        Index(name = "idx_orders_owner_created", columnList = "owner_id, created_at DESC"),
        Index(name = "idx_orders_customer", columnList = "owner_id, customer_id"),
    ],
)
@SQLRestriction("is_active = 1")
class OrderEntity(
    /** User ID của chủ cửa hàng — FK → users(id) */
    @Column(nullable = false)
    val ownerId: UUID,

    /** Số tham chiếu — tự sinh (VD: DH-20260608-001) */
    @Column(nullable = false, length = 50)
    var referenceNumber: String,

    /** FK → customers(id) — nullable nếu khách vãng lai */
    @Column
    var customerId: UUID? = null,

    /** Tổng tiền hàng */
    @Column(nullable = false, precision = 18, scale = 0)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    /** Số tiền đã thanh toán */
    @Column(nullable = false, precision = 18, scale = 0)
    var paidAmount: BigDecimal = BigDecimal.ZERO,

    /** Công nợ = totalAmount - paidAmount */
    @Column(nullable = false, precision = 18, scale = 0)
    var debtAmount: BigDecimal = BigDecimal.ZERO,

    /** Trạng thái: DRAFT, CONFIRMED, CANCELLED */
    @Column(nullable = false, length = 20)
    var status: String = OrderStatus.DRAFT,

    /** Ghi chú */
    @Column(length = 4000)
    var notes: String? = null,

    /** Soft delete flag */
    @Column(nullable = false)
    var isActive: Boolean = true,

    /** Optimistic locking */
    @Version
    @Column(nullable = false)
    var version: Long = 0,

    /** Thời điểm cập nhật cuối */
    @Column
    var updatedAt: Instant? = null,
) : BaseEntity()

/** Trạng thái đơn hàng */
object OrderStatus {
    const val DRAFT = "DRAFT"
    const val CONFIRMED = "CONFIRMED"
    const val CANCELLED = "CANCELLED"
}
