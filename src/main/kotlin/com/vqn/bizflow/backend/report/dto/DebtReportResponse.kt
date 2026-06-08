package com.vqn.bizflow.backend.report.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID

@Schema(description = "Customer debt entry")
data class CustomerDebt(
    @field:Schema(description = "Customer ID (null = walk-in)")
    val customerId: UUID?,

    @field:Schema(description = "Customer name", example = "Nguyễn Văn A")
    val customerName: String,

    @field:Schema(description = "Total outstanding debt (VND)", example = "2500000")
    val totalDebt: BigDecimal,

    @field:Schema(description = "Number of unpaid orders", example = "3")
    val orderCount: Long,
)

@Schema(description = "Debt report — outstanding customer debts")
data class DebtReportResponse(
    @field:Schema(description = "Total outstanding debt across all customers (VND)", example = "12500000")
    val totalDebt: BigDecimal,

    @field:Schema(description = "Customer debt entries ordered by amount descending")
    val customers: List<CustomerDebt>,
)
