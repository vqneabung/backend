package com.vqn.bizflow.backend.inventory.repository

import com.vqn.bizflow.backend.inventory.entity.StockImportItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * Repository cho StockImportItem.
 *
 * Luôn query theo stockImportId (không query độc lập).
 */
interface StockImportItemRepository : JpaRepository<StockImportItemEntity, UUID> {

    /** Lấy tất cả items của 1 phiếu nhập */
    fun findByStockImportIdOrderByCreatedAt(stockImportId: UUID): List<StockImportItemEntity>

    /** Đếm số items — dùng cho summary response */
    fun countByStockImportId(stockImportId: UUID): Int
}
