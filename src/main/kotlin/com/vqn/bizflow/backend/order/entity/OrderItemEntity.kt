package com.vqn.bizflow.backend.order.entity

import com.vqn.bizflow.backend.entity.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

/**
 * Chi tiết đơn hàng — 1 dòng = 1 sản phẩm + số lượng + giá bán.
 *
 * Quan hệ ManyToOne với Order (qua orderId).
 * KHÔNG có cascade — service quản lý lifecycle riêng.
 * product_name + unit_price là snapshot tại thời điểm tạo đơn
 * (bảo vệ khỏi thay đổi sau này của sản phẩm).
 * subtotal = quantity * unit_price.
 */
@Entity
@Table(
    name = "order_items",
    indexes = [
        Index(name = "idx_order_items_order_id", columnList = "order_id"),
        Index(name = "idx_order_items_product_id", columnList = "product_id"),
    ],
)
class OrderItemEntity(
    /** FK → orders(id) */
    @Column(nullable = false)
    val orderId: UUID,

    /** FK → products(id) */
    @Column(nullable = false)
    val productId: UUID,

    /** Tên sản phẩm (snapshot — không đổi theo thời gian) */
    @Column(nullable = false, length = 255)
    val productName: String,

    /** Số lượng bán */
    @Column(nullable = false, precision = 18, scale = 0)
    val quantity: BigDecimal,

    /** Đơn giá bán (snapshot tại thời điểm tạo đơn) */
    @Column(nullable = false, precision = 18, scale = 0)
    val unitPrice: BigDecimal,

    /** Thành tiền = quantity * unitPrice */
    @Column(nullable = false, precision = 18, scale = 0)
    val subtotal: BigDecimal,
) : BaseEntity()
