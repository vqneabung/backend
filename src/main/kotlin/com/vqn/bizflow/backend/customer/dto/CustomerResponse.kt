package com.vqn.bizflow.backend.customer.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Response trả về thông tin khách hàng.
 *
 * Dùng MapStruct (CustomerMapper) để chuyển từ CustomerEntity sang DTO này.
 * totalDebt là snapshot công nợ — cập nhật khi tạo order.
 */
@Schema(description = "Thông tin khách hàng")
data class CustomerResponse(
    @field:Schema(description = "ID khách hàng (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Tên khách hàng", example = "Nguyễn Văn A")
    val name: String,

    @field:Schema(description = "Số điện thoại", example = "0912345678")
    val phone: String?,

    @field:Schema(description = "Email", example = "a@example.com")
    val email: String?,

    @field:Schema(description = "Địa chỉ", example = "123 Nguyễn Huệ, Q1, HCM")
    val address: String?,

    @field:Schema(description = "Ghi chú", example = "Khách quen, thường mua sỉ")
    val notes: String?,

    @field:Schema(description = "Tổng công nợ (VND)", example = "500000")
    val totalDebt: BigDecimal,

    @field:Schema(description = "Còn hoạt động không")
    val isActive: Boolean,

    @field:Schema(description = "Ngày tạo")
    val createdAt: Instant,

    @field:Schema(description = "Ngày cập nhật")
    val updatedAt: Instant?,
)
