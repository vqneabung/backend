package com.vqn.bizflow.backend.report.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "Daily revenue data point for chart")
data class RevenueDailyPoint(
    @field:Schema(description = "Date (yyyy-MM-dd)", example = "2026-06-08")
    val date: String,

    @field:Schema(description = "Revenue on that day (VND)", example = "2500000")
    val revenue: BigDecimal,
)

@Schema(description = "Revenue report (daily or monthly)")
data class RevenueReportResponse(
    @field:Schema(description = "Revenue data points ordered by date")
    val points: List<RevenueDailyPoint>,

    @field:Schema(description = "Total revenue in period (VND)", example = "18500000")
    val total: BigDecimal,

    @field:Schema(description = "Period start (ISO date)", example = "2026-06-01")
    val periodStart: String,

    @field:Schema(description = "Period end (ISO date)", example = "2026-06-08")
    val periodEnd: String,
)
