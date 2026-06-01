package com.vqn.bizflow.backend.product.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Size
import java.math.BigDecimal

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

    @field:Size(max = 100, message = "Category must not exceed 100 characters")
    @field:Schema(description = "Danh mục", example = "VLXD")
    val category: String? = null,

    @field:Size(max = 50, message = "Unit must not exceed 50 characters")
    @field:Schema(description = "Đơn vị tính chính", example = "Bao")
    val primaryUnit: String? = null,

    @field:DecimalMin(value = "1", message = "Price must be greater than 0")
    @field:Schema(description = "Giá bán (VND)", example = "85000")
    val price: BigDecimal? = null,

    @field:DecimalMin(value = "0", message = "Cost price must be greater than or equal to 0")
    @field:Schema(description = "Giá vốn", example = "70000")
    val costPrice: BigDecimal? = null,

    @field:DecimalMin(value = "0", message = "Stock cannot be negative")
    @field:Schema(description = "Tồn kho", example = "150")
    val stock: BigDecimal? = null,

    @field:DecimalMin(value = "0", message = "Min stock cannot be negative")
    @field:Schema(description = "Tồn tối thiểu", example = "20")
    val minStock: BigDecimal? = null,

    @field:Size(max = 500, message = "Image URL must not exceed 500 characters")
    @field:Schema(description = "URL hình ảnh")
    val imageUrl: String? = null,

    @field:Size(max = 100, message = "Barcode must not exceed 100 characters")
    @field:Schema(description = "Mã vạch")
    val barcode: String? = null,
)
