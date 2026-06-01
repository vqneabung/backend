package com.vqn.bizflow.backend.product.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Response trả về thông tin sản phẩm.
 *
 * Dùng MapStruct (ProductMapper) để chuyển từ ProductEntity sang DTO này.
 * isLowStock là computed field: stock < minStock.
 */
@Schema(description = "Thông tin sản phẩm")
data class ProductResponse(
    @field:Schema(description = "ID sản phẩm (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Tên sản phẩm", example = "Xi măng Hà Tiên PCB40")
    val name: String,

    @field:Schema(description = "Danh mục", example = "VLXD")
    val category: String?,

    @field:Schema(description = "Đơn vị tính chính", example = "Bao")
    val primaryUnit: String,

    @field:Schema(description = "Giá bán (VND)", example = "85000")
    val price: BigDecimal,

    @field:Schema(description = "Giá vốn (VND)", example = "70000")
    val costPrice: BigDecimal?,

    @field:Schema(description = "Tồn kho", example = "150")
    val stock: BigDecimal,

    @field:Schema(description = "Tồn tối thiểu", example = "20")
    val minStock: BigDecimal,

    @field:Schema(description = "URL hình ảnh")
    val imageUrl: String?,

    @field:Schema(description = "Mã vạch")
    val barcode: String?,

    @field:Schema(description = "Còn bán không")
    val isActive: Boolean,

    @field:Schema(description = "Cảnh báo tồn kho thấp (stock < minStock)")
    val isLowStock: Boolean,

    @field:Schema(description = "Ngày tạo")
    val createdAt: Instant,

    @field:Schema(description = "Ngày cập nhật")
    val updatedAt: Instant?,
)
