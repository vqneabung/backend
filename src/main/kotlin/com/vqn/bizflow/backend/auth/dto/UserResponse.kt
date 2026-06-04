package com.vqn.bizflow.backend.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

/**
 * UserResponse — Thông tin user trả về cho frontend.
 *
 * Dùng cho endpoint /api/auth/me để lấy thông tin user hiện tại.
 * Không bao gồm password.
 */
@Schema(description = "Thông tin người dùng")
data class UserResponse(
    @field:Schema(description = "UUID của user", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Email người dùng", example = "user@example.com")
    val email: String,

    @field:Schema(description = "Tên hiển thị", example = "Nguyen Van A")
    val name: String?,

    @field:Schema(description = "Vai trò", example = "USER")
    val role: String,

    @field:Schema(description = "Ngày tham gia (ISO-8601)", example = "2026-01-15T10:30:00Z")
    val joinedAt: Instant,
)
