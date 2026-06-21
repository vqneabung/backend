package com.vqn.bizflow.backend.owner.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Yêu cầu tạo nhân viên mới (Owner only)")
data class CreateEmployeeRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    @field:Schema(description = "Email nhân viên", example = "employee@example.com")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    @field:Schema(description = "Mật khẩu khởi tạo (tối thiểu 6 ký tự)", example = "123456")
    val password: String,

    @field:Size(max = 255, message = "Name must be at most 255 characters")
    @field:Schema(description = "Tên hiển thị (optional)", example = "Nguyen Van B")
    val name: String? = null,
)
