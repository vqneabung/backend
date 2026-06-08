package com.vqn.bizflow.backend.report.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID

@Schema(description = "Best-selling product entry")
data class BestSellingProduct(
    @field:Schema(description = "Product ID")
    val productId: UUID,

    @field:Schema(description = "Product name", example = "Xi măng Hà Tiên PCB40")
    val productName: String,

    @field:Schema(description = "Total quantity sold", example = "150")
    val quantitySold: BigDecimal,

    @field:Schema(description = "Total revenue from this product (VND)", example = "12750000")
    val revenue: BigDecimal,
)

@Schema(description = "Best-selling products report (top N)")
data class BestSellingReportResponse(
    @field:Schema(description = "Top products ordered by quantity sold descending")
    val products: List<BestSellingProduct>,
)
