package com.vqn.bizflow.backend.order.dto

import java.util.UUID

/**
 * Request cập nhật trạng thái đơn hàng.
 * Hiện tại chỉ support hủy đơn (cancel).
 */
data class UpdateOrderStatusRequest(
    val status: String,
    val notes: String? = null,
)
