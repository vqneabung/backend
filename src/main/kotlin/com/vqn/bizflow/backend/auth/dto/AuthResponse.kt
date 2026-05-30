package com.vqn.bizflow.backend.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Kết quả xác thực (JWT token)")
data class AuthResponse(
    @field:Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
    val token: String,

    @field:Schema(description = "Email người dùng", example = "user@example.com")
    val email: String,

    @field:Schema(description = "Vai trò người dùng", example = "USER", allowableValues = ["USER", "ADMIN"])
    val role: String,

    @field:Schema(description = "Tên người dùng", example = "Nguyen Van A")
    val name: String? = null
)