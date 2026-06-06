package com.vqn.bizflow.backend.product.entity

import com.vqn.bizflow.backend.entity.BaseEntity
import jakarta.persistence.*
import java.util.UUID

/**
 * Danh mục sản phẩm (toàn cục hoặc do user định nghĩa).
 *
 * - ownerId == null → global category (mọi user đều dùng được)
 * - ownerId != null → user-defined category (riêng user đó)
 *
 * Global categories được seed bởi DataInitializer (VLXD, Tạp hóa, Điện nước...).
 */
@Entity
@Table(
    name = "categories",
    indexes = [
        Index(name = "idx_categories_owner_id", columnList = "owner_id"),
        Index(name = "idx_categories_name_owner", columnList = "name, owner_id", unique = true),
    ]
)
class CategoryEntity(
    /** null = global category, non-null = user-defined */
    @Column
    val ownerId: UUID? = null,

    /** Tên danh mục: VLXD, Tạp hóa... */
    @Column(nullable = false, length = 100)
    var name: String,

    /** Mô tả ngắn (tuỳ chọn) */
    @Column(length = 200)
    var description: String? = null,
) : BaseEntity()
