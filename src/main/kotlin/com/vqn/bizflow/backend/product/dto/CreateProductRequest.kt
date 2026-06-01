package com.vqn.bizflow.backend.product.dto

import com.vqn.bizflow.backend.product.entity.ProductEntity
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.util.UUID

/**
 * Request tạo sản phẩm mới.
 *
 * Validation:
 * - name: required, không blank
 * - primaryUnit: required, không blank
 * - price: required, > 0
 * - stock: >= 0 (mặc định 0)
 * - imageUrl: nếu có phải là URL hợp lệ
 */
@Schema(description = "Yêu cầu tạo sản phẩm mới")
data class CreateProductRequest(
    @field:NotBlank(message = "Product name is required")
    @field:Size(max = 255, message = "Product name must not exceed 255 characters")
    @field:Schema(description = "Tên sản phẩm", example = "Xi măng Hà Tiên PCB40")
    val name: String,

    @field:Size(max = 100, message = "Category must not exceed 100 characters")
    @field:Schema(description = "Danh mục", example = "VLXD")
    val category: String? = null,

    @field:NotBlank(message = "Primary unit is required")
    @field:Size(max = 50, message = "Unit must not exceed 50 characters")
    @field:Schema(description = "Đơn vị tính chính", example = "Bao")
    val primaryUnit: String,

    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "1", message = "Price must be greater than 0")
    @field:Schema(description = "Giá bán (VND)", example = "85000")
    val price: BigDecimal,

    @field:DecimalMin(value = "0", message = "Cost price must be greater than or equal to 0")
    @field:Schema(description = "Giá vốn (tuỳ chọn)", example = "70000")
    val costPrice: BigDecimal? = null,

    @field:DecimalMin(value = "0", message = "Stock cannot be negative")
    @field:Schema(description = "Tồn kho hiện tại", example = "150")
    val stock: BigDecimal? = BigDecimal.ZERO,

    @field:DecimalMin(value = "0", message = "Min stock cannot be negative")
    @field:Schema(description = "Tồn tối thiểu (cảnh báo)", example = "20")
    val minStock: BigDecimal? = BigDecimal.ZERO,

    @field:Schema(description = "URL hình ảnh", example = "https://example.com/ximang.jpg")
    val imageUrl: String? = null,

    @field:Size(max = 100, message = "Barcode must not exceed 100 characters")
    @field:Schema(description = "Mã vạch", example = "8934567890123")
    val barcode: String? = null,
)

/**
 * Extension function: CreateProductRequest → ProductEntity.
 *
 * Tự động trim string fields, set default values.
 * ownerId được inject từ authentication context (không phải từ request).
 */
fun CreateProductRequest.toEntity(ownerId: UUID): ProductEntity = ProductEntity(
    ownerId = ownerId,
    name = name.trim(),
    category = category?.trim(),
    primaryUnit = primaryUnit.trim(),
    price = price,
    costPrice = costPrice,
    stock = stock ?: BigDecimal.ZERO,
    minStock = minStock ?: BigDecimal.ZERO,
    imageUrl = imageUrl?.trim(),
    barcode = barcode?.trim(),
)
