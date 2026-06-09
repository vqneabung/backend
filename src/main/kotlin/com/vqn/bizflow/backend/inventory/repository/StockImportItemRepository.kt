package com.vqn.bizflow.backend.inventory.repository

import com.vqn.bizflow.backend.inventory.entity.StockImportItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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

    /**
     * Batch đếm items cho nhiều phiếu nhập — tránh N+1 query khi build summary list.
     * Returns: List<[stockImportId, count]>
     */
    @Query("SELECT si.stockImportId, COUNT(si) FROM StockImportItemEntity si WHERE si.stockImportId IN :stockImportIds GROUP BY si.stockImportId")
    fun countByStockImportIds(@Param("stockImportIds") stockImportIds: List<UUID>): List<Array<Any>>
}
