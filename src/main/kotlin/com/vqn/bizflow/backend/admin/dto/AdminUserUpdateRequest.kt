package com.vqn.bizflow.backend.admin.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "Yêu cầu cập nhật user (admin)")
data class AdminUserUpdateRequest(
    @field:NotBlank(message = "Name is required")
    @field:Schema(description = "Tên hiển thị", example = "Nguyen Van A")
    val name: String,

    @field:NotNull(message = "Role is required")
    @field:Schema(description = "Vai trò (USER hoặc ADMIN)", example = "USER", allowableValues = ["USER", "ADMIN"])
    val role: String,
)
