package com.vqn.bizflow.backend.customer.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

/**
 * Request cập nhật khách hàng.
 *
 * Tất cả fields đều optional — chỉ gửi field muốn thay đổi.
 */
@Schema(description = "Yêu cầu cập nhật khách hàng")
data class UpdateCustomerRequest(
    @field:Size(max = 255, message = "Customer name must not exceed 255 characters")
    @field:Schema(description = "Tên khách hàng", example = "Nguyễn Văn A")
    val name: String? = null,

    @field:Size(max = 20, message = "Phone must not exceed 20 characters")
    @field:Schema(description = "Số điện thoại", example = "0912345678")
    val phone: String? = null,

    @field:Size(max = 255, message = "Email must not exceed 255 characters")
    @field:Schema(description = "Email", example = "a@example.com")
    val email: String? = null,

    @field:Size(max = 500, message = "Address must not exceed 500 characters")
    @field:Schema(description = "Địa chỉ", example = "123 Nguyễn Huệ, Q1, HCM")
    val address: String? = null,

    @field:Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @field:Schema(description = "Ghi chú", example = "Khách quen, thường mua sỉ")
    val notes: String? = null,
)
