package com.vqn.bizflow.backend.order.repository

import com.vqn.bizflow.backend.order.entity.OrderEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Repository cho OrderEntity.
 *
 * @SQLRestriction(is_active = 1) tự động filter soft-delete.
 * Multi-tenant qua ownerId.
 */
interface OrderRepository : JpaRepository<OrderEntity, UUID> {

    /** List đơn hàng của owner (mới nhất trước, filter trạng thái) */
    @Query("""
        SELECT o FROM OrderEntity o
        WHERE o.ownerId = :ownerId
        AND (:status IS NULL OR o.status = :status)
        AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
        AND (:toDate IS NULL OR o.createdAt <= :toDate)
    """)
    fun findByOwnerId(
        @Param("ownerId") ownerId: UUID,
        @Param("status") status: String?,
        @Param("fromDate") fromDate: Instant?,
        @Param("toDate") toDate: Instant?,
        pageable: Pageable,
    ): Page<OrderEntity>

    /** Tìm đơn hàng của 1 owner (kiểm tra ownership) */
    fun findByIdAndOwnerId(id: UUID, ownerId: UUID): OrderEntity?

    /** List đơn hàng theo khách hàng — dùng cho Customer Purchase History (FR-17) */
    @Query("""
        SELECT o FROM OrderEntity o
        WHERE o.ownerId = :ownerId AND o.customerId = :customerId
        ORDER BY o.createdAt DESC
    """)
    fun findByOwnerIdAndCustomerId(
        @Param("ownerId") ownerId: UUID,
        @Param("customerId") customerId: UUID,
        pageable: Pageable,
    ): Page<OrderEntity>

    /** Đếm số đơn hôm nay (dùng cho sinh reference number) */
    fun countByOwnerIdAndCreatedAtBetween(
        ownerId: UUID,
        start: Instant,
        end: Instant,
    ): Long

    // ═══════════════════════════════════════════════
    // Report queries
    // ═══════════════════════════════════════════════

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o WHERE o.ownerId = :ownerId AND o.status = :status AND o.createdAt BETWEEN :fromDate AND :toDate")
    fun sumTotalAmountByOwnerIdAndStatusAndCreatedAtBetween(
        @Param("ownerId") ownerId: UUID,
        @Param("status") status: String,
        @Param("fromDate") fromDate: Instant,
        @Param("toDate") toDate: Instant,
    ): BigDecimal

    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.ownerId = :ownerId AND o.status = :status AND o.createdAt BETWEEN :fromDate AND :toDate")
    fun countByOwnerIdAndStatusAndCreatedAtBetween(
        @Param("ownerId") ownerId: UUID,
        @Param("status") status: String,
        @Param("fromDate") fromDate: Instant,
        @Param("toDate") toDate: Instant,
    ): Long

    @Query("SELECT o FROM OrderEntity o WHERE o.ownerId = :ownerId AND o.status = :status AND o.createdAt BETWEEN :fromDate AND :toDate ORDER BY o.createdAt ASC")
    fun findByOwnerIdAndStatusAndCreatedAtBetween(
        @Param("ownerId") ownerId: UUID,
        @Param("status") status: String,
        @Param("fromDate") fromDate: Instant,
        @Param("toDate") toDate: Instant,
    ): List<OrderEntity>
}
