package com.vqn.bizflow.backend.inventory.dto

import com.vqn.bizflow.backend.inventory.entity.StockImportEntity
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Response chi tiết phiếu nhập kho (kèm danh sách items).
 */
data class StockImportResponse(
    val id: UUID,
    val ownerId: UUID,
    val referenceNumber: String,
    val supplier: String?,
    val notes: String?,
    val importDate: Instant,
    val totalCost: BigDecimal,
    val itemCount: Int,
    val items: List<StockImportItemResponse>,
    val createdAt: Instant,
    val updatedAt: Instant?,
)

/**
 * Response 1 dòng trong phiếu nhập.
 */
data class StockImportItemResponse(
    val id: UUID,
    val productId: UUID,
    val productName: String,
    val quantity: BigDecimal,
    val unitCost: BigDecimal,
    val subtotal: BigDecimal,
)

/**
 * Response list (không kèm items — dùng cho bảng danh sách).
 */
data class StockImportSummaryResponse(
    val id: UUID,
    val referenceNumber: String,
    val supplier: String?,
    val importDate: Instant,
    val totalCost: BigDecimal,
    val itemCount: Int,
    val createdAt: Instant,
) {
    companion object {
        fun from(entity: StockImportEntity, itemCount: Int) = StockImportSummaryResponse(
            id = entity.id!!,
            referenceNumber = entity.referenceNumber,
            supplier = entity.supplier,
            importDate = entity.importDate,
            totalCost = entity.totalCost,
            itemCount = itemCount,
            createdAt = entity.createdAt,
        )
    }
}
