package com.vqn.bizflow.backend.platform.entity

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

/**
 * Thông báo nền tảng hiển thị cho user (system-wide, không gắn với owner cụ thể).
 *
 * - audience: "all" | "owner" | "employee" — lọc phía repository bằng IN clause.
 * - isPublished = false → announcement nháp, KHÔNG trả cho frontend.
 * - publishedAt set khi lần đầu publish; expiresAt set nếu có hạn.
 *
 * updatedAt khai báo riêng để tránh Hibernate 7 auto-detect timestamp field
 * làm optimistic lock.
 */
@Entity
@Table(
    name = "announcements",
    indexes = [
        Index(name = "idx_announcements_audience_published", columnList = "audience, is_published, published_at"),
        Index(name = "idx_announcements_is_published", columnList = "is_published"),
    ],
)
class AnnouncementEntity(
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID? = null,

    @Column(nullable = false, length = 255)
    var title: String = "",

    @Column(nullable = false, columnDefinition = "nvarchar(max)")
    var message: String = "",

    @Column(nullable = false, length = 30)
    var audience: String = "all",

    @Column(nullable = false, length = 20)
    var priority: String = "normal",

    @Column(name = "is_published", nullable = false)
    var isPublished: Boolean = false,

    @Column(name = "published_at")
    var publishedAt: Instant? = null,

    @Column(name = "expires_at")
    var expiresAt: Instant? = null,

    @Column(name = "created_by", length = 255)
    var createdBy: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(name = "updated_at")
    var updatedAt: Instant? = null,
)
