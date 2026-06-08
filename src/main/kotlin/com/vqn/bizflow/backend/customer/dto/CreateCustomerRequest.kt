package com.vqn.bizflow.backend.customer.dto

import com.vqn.bizflow.backend.customer.entity.CustomerEntity
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

/**
 * Request tạo khách hàng mới.
 *
 * Validation:
 * - name: required, không blank, max 255
 * - phone: optional, max 20
 * - email: optional, max 255
 * - address: optional, max 500
 * - notes: optional, max 1000
 */
@Schema(description = "Yêu cầu tạo khách hàng mới")
data class CreateCustomerRequest(
    @field:NotBlank(message = "Customer name is required")
    @field:Size(max = 255, message = "Customer name must not exceed 255 characters")
    @field:Schema(description = "Tên khách hàng", example = "Nguyễn Văn A")
    val name: String,

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

/** Extension function: CreateCustomerRequest → CustomerEntity */
fun CreateCustomerRequest.toEntity(ownerId: UUID): CustomerEntity = CustomerEntity(
    ownerId = ownerId,
    name = name.trim(),
    phone = phone?.trim(),
    email = email?.trim(),
    address = address?.trim(),
    notes = notes?.trim(),
)
