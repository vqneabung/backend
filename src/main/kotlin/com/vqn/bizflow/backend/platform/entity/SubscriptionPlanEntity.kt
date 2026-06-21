package com.vqn.bizflow.backend.platform.entity

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Gói subscription cho Owner.
 *
 * - slug là định danh duy nhất (URL-friendly, vd: "free", "pro", "enterprise")
 * - features lưu dưới dạng JSON string (List<String>) — parse trong mapper.
 *
 * updatedAt khai báo riêng để tránh Hibernate 7 auto-detect timestamp field
 * làm optimistic lock (xem BaseEntity comment).
 */
@Entity
@Table(
    name = "subscription_plans",
    indexes = [
        Index(name = "idx_subscription_plans_slug", columnList = "slug", unique = true),
        Index(name = "idx_subscription_plans_is_active_sort", columnList = "is_active, sort_order"),
    ],
)
class SubscriptionPlanEntity(
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID? = null,

    @Column(nullable = false, length = 100)
    var name: String = "",

    @Column(nullable = false, unique = true, length = 100)
    var slug: String = "",

    @Column(name = "monthly_price", nullable = false, precision = 18, scale = 0)
    var monthlyPrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "annual_price", nullable = false, precision = 18, scale = 0)
    var annualPrice: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, length = 3)
    var currency: String = "VND",

    /** JSON array of feature strings — parsed by mapper */
    @Column(columnDefinition = "nvarchar(max)")
    var features: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(name = "updated_at")
    var updatedAt: Instant? = null,
)
