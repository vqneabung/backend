package com.vqn.bizflow.backend.product.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Request tạo đơn vị tính mới (user-defined).
 *
 * Backed by POST /api/reference/units.
 * Gửi JSON body thay vì form-encoded để tránh lỗi @RequestParam mismatch.
 */
@Schema(description = "Yêu cầu tạo đơn vị tính mới")
data class CreateUnitRequest(
    @field:NotBlank(message = "Unit name is required")
    @field:Size(max = 50, message = "Unit name must not exceed 50 characters")
    @field:Schema(description = "Tên đơn vị tính", example = "Cây")
    val name: String,

    @field:Size(max = 255, message = "Description must not exceed 255 characters")
    @field:Schema(description = "Mô tả (tuỳ chọn)", example = "Dùng cho sắt thép")
    val description: String? = null,
)
