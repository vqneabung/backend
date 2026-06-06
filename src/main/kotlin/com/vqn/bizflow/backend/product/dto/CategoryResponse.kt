package com.vqn.bizflow.backend.product.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

/**
 * Response cho danh mục sản phẩm.
 */
@Schema(description = "Danh mục sản phẩm")
data class CategoryResponse(
    @field:Schema(description = "ID (UUID)")
    val id: UUID,

    @field:Schema(description = "Tên danh mục", example = "VLXD")
    val name: String,

    @field:Schema(description = "Mô tả")
    val description: String?,

    @field:Schema(description = "Global hay user-defined (null = global)")
    val ownerId: UUID?,
)
