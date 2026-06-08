package com.vqn.bizflow.backend.report.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "Overview statistics dashboard")
data class ReportOverviewResponse(
    @field:Schema(description = "Total active products", example = "42")
    val totalProducts: Long,

    @field:Schema(description = "Total orders this month (CONFIRMED)", example = "18")
    val totalOrdersThisMonth: Long,

    @field:Schema(description = "Total revenue this month (VND)", example = "18500000")
    val totalRevenueThisMonth: BigDecimal,

    @field:Schema(description = "Total customers", example = "25")
    val totalCustomers: Long,

    @field:Schema(description = "Products with stock <= minStock", example = "3")
    val lowStockCount: Long,
)
