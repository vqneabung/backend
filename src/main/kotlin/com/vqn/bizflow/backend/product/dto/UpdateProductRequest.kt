package com.vqn.bizflow.backend.product.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.util.UUID

/**
 * Request cập nhật sản phẩm.
 *
 * Tất cả fields đều optional — chỉ gửi field muốn thay đổi.
 * Validation chỉ chạy nếu field có giá trị.
 */
@Schema(description = "Yêu cầu cập nhật sản phẩm")
data class UpdateProductRequest(
    @field:Size(max = 255, message = "Product name must not exceed 255 characters")
    @field:Schema(description = "Tên sản phẩm", example = "Xi măng Hà Tiên PCB40")
    val name: String? = null,

    @field:Schema(description = "ID danh mục (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    val categoryId: UUID? = null,

    @field:Schema(description = "ID đơn vị tính chính (UUID)", example = "550e8400-e29b-41d4-a716-446655440001")
    val primaryUnitId: UUID? = null,

    @field:DecimalMin(value = "1", message = "Price must be greater than 0")
    @field:Schema(description = "Giá bán (VND)", example = "85000")
    val price: BigDecimal? = null,

    @field:DecimalMin(value = "0", message = "Cost price must be greater than or equal to 0")
    @field:Schema(description = "Giá vốn", example = "70000")
    val costPrice: BigDecimal? = null,

    @field:DecimalMin(value = "0", message = "Stock cannot be negative")
    @field:Schema(description = "Tồn kho (số nguyên)", example = "150")
    val stock: BigDecimal? = null,

    @field:DecimalMin(value = "0", message = "Min stock cannot be negative")
    @field:Schema(description = "Tồn tối thiểu (số nguyên)", example = "20")
    val minStock: BigDecimal? = null,

    @field:Size(max = 500, message = "Image URL must not exceed 500 characters")
    @field:Schema(description = "URL hình ảnh (external URL hoặc fallback)")
    val imageUrl: String? = null,

    @field:Schema(description = "Danh sách MinIO objectKey mới (replace toàn bộ — tối đa 5)")
    val imageKeys: List<String>? = null,

    @field:Size(max = 100, message = "Barcode must not exceed 100 characters")
    @field:Schema(description = "Mã vạch")
    val barcode: String? = null,
)
