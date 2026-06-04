package com.vqn.bizflow.backend.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

@Schema(description = "Kết quả xác thực (JWT token)")
data class AuthResponse(
    @field:Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
    val token: String,

    @field:Schema(description = "Email người dùng", example = "user@example.com")
    val email: String,

    @field:Schema(description = "Vai trò người dùng", example = "USER")
    val role: String,

    @field:Schema(description = "Tên người dùng", example = "Nguyen Van A")
    val name: String? = null,

    @field:Schema(description = "UUID của user", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID? = null,

    @field:Schema(description = "Ngày tham gia (ISO-8601)", example = "2026-01-15T10:30:00Z")
    val joinedAt: Instant? = null,
)