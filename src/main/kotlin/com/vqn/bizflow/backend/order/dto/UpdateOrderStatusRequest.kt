package com.vqn.bizflow.backend.order.dto

import java.util.UUID

/**
 * Request cập nhật trạng thái đơn hàng.
 * Hiện tại chỉ support hủy đơn (cancel) — `status` luôn = CANCELLED.
 * Field `status` đã bỏ vì service không đọc (giảm confusion cho client).
 */
data class UpdateOrderStatusRequest(
    val notes: String? = null,
)
