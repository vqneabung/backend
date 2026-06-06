package com.vqn.bizflow.backend.product.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

/**
 * Response cho đơn vị tính.
 */
@Schema(description = "Đơn vị tính")
data class UnitResponse(
    @field:Schema(description = "ID (UUID)")
    val id: UUID,

    @field:Schema(description = "Tên đơn vị", example = "Bao")
    val name: String,

    @field:Schema(description = "Mô tả")
    val description: String?,

    @field:Schema(description = "Global hay user-defined (null = global)")
    val ownerId: UUID?,
)
