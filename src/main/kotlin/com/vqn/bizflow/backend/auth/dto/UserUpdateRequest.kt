package com.vqn.bizflow.backend.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Cập nhật thông tin người dùng")
data class UserUpdateRequest(
    @field:Schema(description = "Tên hiển thị mới", example = "Nguyen Van A")
    val name: String? = null,
)
