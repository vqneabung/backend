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
 * - primaryUnitId: required
 * - price: required, > 0
 * - stock: >= 0 (mặc định 0), integer
 * - imageUrl: nếu có phải là URL hợp lệ
 */
@Schema(description = "Yêu cầu tạo sản phẩm mới")
data class CreateProductRequest(
    @field:NotBlank(message = "Product name is required")
    @field:Size(max = 255, message = "Product name must not exceed 255 characters")
    @field:Schema(description = "Tên sản phẩm", example = "Xi măng Hà Tiên PCB40")
    val name: String,

    @field:Schema(description = "ID danh mục (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    val categoryId: UUID? = null,

    @field:NotNull(message = "Primary unit is required")
    @field:Schema(description = "ID đơn vị tính chính (UUID)", example = "550e8400-e29b-41d4-a716-446655440001")
    val primaryUnitId: UUID,

    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "1", message = "Price must be greater than 0")
    @field:Schema(description = "Giá bán (VND)", example = "85000")
    val price: BigDecimal,

    @field:DecimalMin(value = "0", message = "Cost price must be greater than or equal to 0")
    @field:Schema(description = "Giá vốn (tuỳ chọn)", example = "70000")
    val costPrice: BigDecimal? = null,

    @field:DecimalMin(value = "0", message = "Stock cannot be negative")
    @field:Schema(description = "Tồn kho hiện tại (số nguyên)", example = "150")
    val stock: BigDecimal? = BigDecimal.ZERO,

    @field:DecimalMin(value = "0", message = "Min stock cannot be negative")
    @field:Schema(description = "Tồn tối thiểu (số nguyên, cảnh báo)", example = "20")
    val minStock: BigDecimal? = BigDecimal.ZERO,

    @field:Schema(description = "URL hình ảnh (external URL hoặc fallback)", example = "https://example.com/ximang.jpg")
    val imageUrl: String? = null,

    @field:Schema(description = "Danh sách MinIO objectKey — frontend upload trước, pass key vào đây (tối đa 5)")
    val imageKeys: List<String> = emptyList(),

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
    categoryId = categoryId,
    primaryUnitId = primaryUnitId,
    price = price,
    costPrice = costPrice,
    stock = stock ?: BigDecimal.ZERO,
    minStock = minStock ?: BigDecimal.ZERO,
    imageUrl = imageUrl?.trim(),
    barcode = barcode?.trim(),
)
