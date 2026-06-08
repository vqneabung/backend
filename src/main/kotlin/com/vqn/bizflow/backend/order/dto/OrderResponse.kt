package com.vqn.bizflow.backend.order.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Response chi tiết đơn hàng (kèm danh sách items).
 */
data class OrderResponse(
    val id: UUID,
    val ownerId: UUID,
    val customerId: UUID?,
    val referenceNumber: String,
    val totalAmount: BigDecimal,
    val paidAmount: BigDecimal,
    val debtAmount: BigDecimal,
    val status: String,
    val notes: String?,
    val itemCount: Int,
    val items: List<OrderItemResponse>,
    val createdAt: Instant,
    val updatedAt: Instant?,
)

/**
 * Response 1 dòng trong đơn hàng.
 */
data class OrderItemResponse(
    val id: UUID,
    val productId: UUID,
    val productName: String,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal,
)

/**
 * Response list (không kèm items — dùng cho bảng danh sách).
 */
data class OrderSummaryResponse(
    val id: UUID,
    val customerId: UUID?,
    val referenceNumber: String,
    val totalAmount: BigDecimal,
    val paidAmount: BigDecimal,
    val debtAmount: BigDecimal,
    val status: String,
    val itemCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant?,
)
