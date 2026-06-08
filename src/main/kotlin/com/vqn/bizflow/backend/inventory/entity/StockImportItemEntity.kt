package com.vqn.bizflow.backend.inventory.entity

import com.vqn.bizflow.backend.entity.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

/**
 * Chi tiết phiếu nhập kho — 1 dòng = 1 sản phẩm + số lượng + giá nhập.
 *
 * Quan hệ ManyToOne với StockImport (qua stockImportId).
 * KHÔNG có cascade — service quản lý lifecycle riêng.
 * subtotal = quantity * unit_cost (tính ở tầng service/DB).
 */
@Entity
@Table(
    name = "stock_import_items",
    indexes = [
        Index(name = "idx_stock_import_items_import_id", columnList = "stock_import_id"),
        Index(name = "idx_stock_import_items_product_id", columnList = "product_id"),
    ],
)
class StockImportItemEntity(
    /** FK → stock_imports(id) */
    @Column(nullable = false)
    val stockImportId: UUID,

    /** FK → products(id) */
    @Column(nullable = false)
    val productId: UUID,

    /** Số lượng nhập (phải > 0) */
    @Column(nullable = false, precision = 18, scale = 0)
    val quantity: BigDecimal,

    /** Đơn giá nhập (phải > 0) */
    @Column(nullable = false, precision = 18, scale = 0)
    val unitCost: BigDecimal,

    /** Thành tiền = quantity * unitCost */
    @Column(nullable = false, precision = 18, scale = 0)
    val subtotal: BigDecimal,
) : BaseEntity()
