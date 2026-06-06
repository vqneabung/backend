package com.vqn.bizflow.backend.product.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Response trả về thông tin sản phẩm.
 *
 * Dùng MapStruct (ProductMapper) để chuyển từ ProductEntity sang DTO này.
 * categoryId, categoryName, primaryUnitId, primaryUnitName được map từ
 * @ManyToOne read-only references trên entity.
 * isLowStock là computed field: stock < minStock.
 */
@Schema(description = "Thông tin sản phẩm")
data class ProductResponse(
    @field:Schema(description = "ID sản phẩm (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Tên sản phẩm", example = "Xi măng Hà Tiên PCB40")
    val name: String,

    @field:Schema(description = "ID danh mục (UUID)")
    val categoryId: UUID?,

    @field:Schema(description = "Tên danh mục", example = "VLXD")
    val categoryName: String?,

    @field:Schema(description = "ID đơn vị tính chính (UUID)")
    val primaryUnitId: UUID,

    @field:Schema(description = "Tên đơn vị tính chính", example = "Bao")
    val primaryUnitName: String,

    @field:Schema(description = "Giá bán (VND)", example = "85000")
    val price: BigDecimal,

    @field:Schema(description = "Giá vốn (VND)", example = "70000")
    val costPrice: BigDecimal?,

    @field:Schema(description = "Tồn kho (số nguyên)", example = "150")
    val stock: BigDecimal,

    @field:Schema(description = "Tồn tối thiểu (số nguyên)", example = "20")
    val minStock: BigDecimal,

    @field:Schema(description = "URL hình ảnh (external hoặc fallback — dùng imageKeys cho upload mới)")
    val imageUrl: String?,

    @field:Schema(description = "Danh sách MinIO objectKey của ảnh (tối đa 5, đã sắp xếp theo position)")
    val imageKeys: List<String> = emptyList(),

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
