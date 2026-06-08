package com.vqn.bizflow.backend.inventory.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Request tạo phiếu nhập kho.
 *
 * items phải có ít nhất 1 phần tử.
 * Nếu referenceNumber để trống, service tự sinh.
 */
data class CreateStockImportRequest(
    val referenceNumber: String? = null,
    val supplier: String? = null,
    val notes: String? = null,
    val importDate: Instant? = null,

    @field:NotEmpty(message = "At least one item is required")
    val items: List<CreateStockImportItemRequest>,
)

data class CreateStockImportItemRequest(
    @field:NotEmpty
    val productId: UUID,

    @field:Positive(message = "Quantity must be positive")
    val quantity: BigDecimal,

    @field:Positive(message = "Unit cost must be positive")
    val unitCost: BigDecimal,
)
