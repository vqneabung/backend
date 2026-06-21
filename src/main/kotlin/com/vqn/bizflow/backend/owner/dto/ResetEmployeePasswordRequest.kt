package com.vqn.bizflow.backend.owner.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@io.swagger.v3.oas.annotations.media.Schema(description = "Yêu cầu đặt lại mật khẩu nhân viên (Owner only)")
data class ResetEmployeePasswordRequest(
    @field:NotBlank(message = "New password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    @field:io.swagger.v3.oas.annotations.media.Schema(description = "Mật khẩu mới (tối thiểu 6 ký tự)", example = "newpass123")
    val newPassword: String,
)
