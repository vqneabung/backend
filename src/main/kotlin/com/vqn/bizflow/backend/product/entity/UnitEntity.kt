package com.vqn.bizflow.backend.product.entity

import com.vqn.bizflow.backend.entity.BaseEntity
import jakarta.persistence.*
import java.util.UUID

/**
 * Đơn vị tính (toàn cục hoặc do user định nghĩa).
 *
 * - ownerId == null → global unit (mọi user đều dùng được)
 * - ownerId != null → user-defined unit (riêng user đó)
 *
 * Global units được seed bởi DataInitializer (Bao, Kg, Thùng, Cái, Mét, Lít, Chai, Hộp).
 */
@Entity
@Table(
    name = "units",
    indexes = [
        Index(name = "idx_units_owner_id", columnList = "owner_id"),
        Index(name = "idx_units_name_owner", columnList = "name, owner_id", unique = true),
    ]
)
class UnitEntity(
    /** null = global unit, non-null = user-defined */
    @Column
    val ownerId: UUID? = null,

    /** Tên đơn vị: Bao, Kg, Thùng... */
    @Column(nullable = false, length = 50)
    var name: String,

    /** Mô tả ngắn (tuỳ chọn) */
    @Column(length = 200)
    var description: String? = null,
) : BaseEntity()
