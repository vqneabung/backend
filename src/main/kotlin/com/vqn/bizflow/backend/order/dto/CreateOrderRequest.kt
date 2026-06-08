package com.vqn.bizflow.backend.order.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.util.UUID

/**
 * Request tạo đơn hàng.
 *
 * Nếu status = "CONFIRMED", hệ thống tự động trừ kho (FR-14).
 * Nếu status = "DRAFT", lưu nháp (không trừ kho).
 * customerId null = khách vãng lai.
 */
data class CreateOrderRequest(
    val customerId: UUID? = null,
    val notes: String? = null,
    val status: String = "DRAFT",

    @field:Valid
    val items: List<CreateOrderItemRequest>,
)

/**
 * 1 dòng sản phẩm trong đơn hàng.
 */
data class CreateOrderItemRequest(
    val productId: UUID,

    @field:Positive(message = "Quantity must be positive")
    val quantity: BigDecimal,

    @field:Positive(message = "Unit price must be positive")
    val unitPrice: BigDecimal,
)
