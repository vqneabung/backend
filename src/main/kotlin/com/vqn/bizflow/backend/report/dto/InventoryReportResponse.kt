package com.vqn.bizflow.backend.report.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID

@Schema(description = "Product category count entry")
data class CategoryCount(
    @field:Schema(description = "Category name", example = "Vật liệu xây dựng")
    val categoryName: String?,

    @field:Schema(description = "Number of products in this category", example = "12")
    val count: Long,
)

@Schema(description = "Low stock product entry")
data class LowStockProduct(
    @field:Schema(description = "Product ID")
    val productId: UUID,

    @field:Schema(description = "Product name", example = "Xi măng Hà Tiên PCB40")
    val productName: String,

    @field:Schema(description = "Current stock", example = "5")
    val stock: BigDecimal,

    @field:Schema(description = "Minimum allowable stock", example = "20")
    val minStock: BigDecimal,
)

@Schema(description = "Inventory report")
data class InventoryReportResponse(
    @field:Schema(description = "Total active products", example = "42")
    val totalProducts: Long,

    @field:Schema(description = "Total inventory value (sum of stock * costPrice) (VND)", example = "85000000")
    val totalValue: BigDecimal,

    @field:Schema(description = "Products with stock below minimum threshold")
    val lowStockProducts: List<LowStockProduct>,

    @field:Schema(description = "Product count grouped by category")
    val byCategory: List<CategoryCount>,
)
