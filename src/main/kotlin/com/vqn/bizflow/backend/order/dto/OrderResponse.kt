package com.vqn.bizflow.backend.order.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Response chi tiết đơn hàng (kèm danh sách items).
 *
 * `items` và `itemCount` có default value để tránh NPE khi MapStruct
 * generate code pass null cho 2 field này (do @Mapping ignore = true
 * trong OrderMapper.toResponse). Kotlin compiler KHÔNG add null check
 * cho params có default, nên constructor từ Java vẫn pass được null.
 *
 * Caller nên dùng OrderMapper.toDetailResponse(entity, items) thay vì
 * toResponse(entity) trực tiếp để có data đầy đủ.
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
    val itemCount: Int = 0,
    val items: List<OrderItemResponse> = emptyList(),
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
