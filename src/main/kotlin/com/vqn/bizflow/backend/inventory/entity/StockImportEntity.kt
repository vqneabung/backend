package com.vqn.bizflow.backend.inventory.entity

import com.vqn.bizflow.backend.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Phiếu nhập kho — ghi nhận lần nhập hàng của Owner.
 *
 * Khi tạo, hệ thống tự động tăng `product.stock` tương ứng.
 * Soft delete: is_active = false thay vì xóa vật lý (giữ audit trail).
 * KHÔNG support edit sau khi tạo — stock đã cập nhật vào product.
 * Multi-tenant qua ownerId.
 */
@Entity
@Table(
    name = "stock_imports",
    indexes = [
        Index(name = "idx_stock_imports_owner_id", columnList = "owner_id"),
    ],
)
@SQLRestriction("is_active = 1")
class StockImportEntity(
    /** User ID của chủ cửa hàng — FK → users(id) */
    @Column(nullable = false)
    val ownerId: UUID,

    /** Số tham chiếu — tự sinh nếu không nhập (VD: NK-20260608-001) */
    @Column(nullable = false, length = 50)
    var referenceNumber: String,

    /** Nhà cung cấp */
    @Column(length = 255)
    var supplier: String? = null,

    /** Ghi chú */
    @Column(length = 4000)
    var notes: String? = null,

    /** Ngày nhập kho */
    @Column(nullable = false)
    var importDate: Instant = Instant.now(),

    /** Tổng giá trị nhập (tính từ items) */
    @Column(nullable = false, precision = 18, scale = 0)
    var totalCost: BigDecimal = BigDecimal.ZERO,

    /** Soft delete flag */
    @Column(nullable = false)
    var isActive: Boolean = true,

    /** Optimistic locking */
    @Version
    @Column(nullable = false)
    var version: Long = 0,

    /** Thời điểm cập nhật cuối */
    @Column
    var updatedAt: Instant? = null,
) : BaseEntity()
