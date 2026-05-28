package com.vqn.bizflow.backend.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Yêu cầu đăng ký tài khoản")
data class RegisterRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    @field:Schema(description = "Email người dùng", example = "user@example.com")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    @field:Schema(description = "Mật khẩu (tối thiểu 6 ký tự)", example = "123456")
    val password: String,

    @field:Schema(description = "Vai trò người dùng (USER hoặc ADMIN)", example = "USER", allowableValues = ["USER", "ADMIN"])
    val role: String? = null
)