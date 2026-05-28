package com.vqn.bizflow.backend.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Yêu cầu đăng nhập")
data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Schema(description = "Email người dùng", example = "user@example.com")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Schema(description = "Mật khẩu", example = "123456")
    val password: String
)