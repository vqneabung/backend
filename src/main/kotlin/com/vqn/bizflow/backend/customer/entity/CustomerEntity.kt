package com.vqn.bizflow.backend.customer.entity

import com.vqn.bizflow.backend.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Khách hàng của chủ cửa hàng (Owner).
 *
 * Soft delete: is_active = false thay vì xóa vật lý.
 * Optimistic locking: @Version để tránh race condition.
 * total_debt là snapshot công nợ — cập nhật khi tạo order.
 *
 * Multi-tenant qua ownerId: mỗi owner chỉ thấy customer của mình.
 */
@Entity
@Table(
    name = "customers",
    indexes = [
        Index(name = "idx_customers_owner_id", columnList = "owner_id"),
        Index(name = "idx_customers_owner_name", columnList = "owner_id, name"),
    ]
)
@SQLRestriction("is_active = 1")
class CustomerEntity(
    /** User ID của chủ cửa hàng — FK → users(id) */
    @Column(nullable = false)
    val ownerId: UUID,

    /** Tên khách hàng — không unique, cùng owner có thể trùng tên */
    @Column(nullable = false, length = 255)
    var name: String,

    /** Số điện thoại */
    @Column(length = 20)
    var phone: String? = null,

    /** Email */
    @Column(length = 255)
    var email: String? = null,

    /** Địa chỉ */
    @Column(length = 500)
    var address: String? = null,

    /** Ghi chú */
    @Column(length = 1000)
    var notes: String? = null,

    /** Tổng công nợ (snapshot — cập nhật khi tạo order) */
    @Column(nullable = false, precision = 18, scale = 0)
    var totalDebt: BigDecimal = BigDecimal.ZERO,

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
