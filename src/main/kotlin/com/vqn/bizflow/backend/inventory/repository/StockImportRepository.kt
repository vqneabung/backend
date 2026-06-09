package com.vqn.bizflow.backend.inventory.repository

import com.vqn.bizflow.backend.inventory.entity.StockImportEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * Repository cho StockImport.
 *
 * Luôn filter theo ownerId (multi-tenant).
 * @SQLRestriction tự động filter is_active = 1.
 */
interface StockImportRepository : JpaRepository<StockImportEntity, UUID> {

    /** List phiếu nhập của 1 owner (phân trang, sort qua Pageable) */
    fun findByOwnerId(ownerId: UUID, pageable: Pageable): Page<StockImportEntity>

    /** Tìm 1 phiếu nhập theo owner (security check) */
    fun findByIdAndOwnerId(id: UUID, ownerId: UUID): StockImportEntity?

    /** Đếm số phiếu nhập hôm nay (sinh reference number) */
    fun countByOwnerIdAndCreatedAtBetween(ownerId: UUID, start: java.time.Instant, end: java.time.Instant): Long

    /** Kiểm tra quyền truy cập (exists check nhanh) */
    fun existsByIdAndOwnerId(id: UUID, ownerId: UUID): Boolean
}
