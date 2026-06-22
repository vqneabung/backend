package com.vqn.bizflow.backend.inventory.entity

import com.vqn.bizflow.backend.entity.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(
    name = "inventory_history",
    indexes = [
        Index(name = "idx_inventory_history_product", columnList = "product_id, created_at DESC"),
        Index(name = "idx_inventory_history_owner", columnList = "owner_id, created_at DESC"),
    ],
)
class InventoryHistoryEntity(
    @Column(name = "owner_id", nullable = false)
    val ownerId: UUID,

    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    @Column(name = "movement_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    val movementType: MovementType,

    @Column(name = "quantity", nullable = false, precision = 18, scale = 0)
    val quantity: BigDecimal,

    @Column(name = "balance_after", nullable = false, precision = 18, scale = 0)
    val balanceAfter: BigDecimal,

    @Column(name = "ref_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val refType: RefType,

    @Column(name = "ref_id", nullable = false)
    val refId: UUID,

    @Column(name = "reference_number", length = 50)
    val referenceNumber: String? = null,
) : BaseEntity()

enum class MovementType { IN, OUT, RETURN }
enum class RefType { STOCK_IMPORT, ORDER }
