package com.vqn.bizflow.backend.product.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Request tạo danh mục mới (user-defined).
 *
 * Backed by POST /api/reference/categories.
 * Gửi JSON body thay vì form-encoded để tránh lỗi @RequestParam mismatch.
 */
@Schema(description = "Yêu cầu tạo danh mục mới")
data class CreateCategoryRequest(
    @field:NotBlank(message = "Category name is required")
    @field:Size(max = 100, message = "Category name must not exceed 100 characters")
    @field:Schema(description = "Tên danh mục", example = "VLXD Nâng cao")
    val name: String,

    @field:Size(max = 255, message = "Description must not exceed 255 characters")
    @field:Schema(description = "Mô tả (tuỳ chọn)", example = "Danh mục cho sản phẩm VLXD cao cấp")
    val description: String? = null,
)
