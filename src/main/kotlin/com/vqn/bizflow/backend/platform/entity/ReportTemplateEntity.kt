package com.vqn.bizflow.backend.platform.entity

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

/**
 * Template báo cáo theo Thông tư 88/2021/TT-BTC (mặc định).
 *
 * - code là định danh kỹ thuật duy nhất (vd: "BCKH_BANHANG")
 * - circularRef mặc định "Circular 88/2021/TT-BTC" nhưng có thể override
 *   nếu Bộ Tài chính ban hành thông tư mới.
 * - fields lưu JSON (List<ReportTemplateField>) — parse trong mapper.
 *
 * updatedAt khai báo riêng để tránh Hibernate 7 auto-detect timestamp field
 * làm optimistic lock.
 */
@Entity
@Table(
    name = "report_templates",
    indexes = [
        Index(name = "idx_report_templates_code", columnList = "code", unique = true),
        Index(name = "idx_report_templates_is_active", columnList = "is_active"),
    ],
)
class ReportTemplateEntity(
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID? = null,

    @Column(nullable = false, length = 255)
    var name: String = "",

    @Column(nullable = false, unique = true, length = 100)
    var code: String = "",

    @Column(length = 1000)
    var description: String? = null,

    @Column(name = "circular_ref", nullable = false, length = 100)
    var circularRef: String = "Circular 88/2021/TT-BTC",

    @Column(nullable = false, length = 20)
    var version: String = "",

    /** JSON array of ReportTemplateField — parsed by mapper */
    @Column(columnDefinition = "nvarchar(max)")
    var fields: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "last_updated_by", length = 255)
    var lastUpdatedBy: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(name = "updated_at")
    var updatedAt: Instant? = null,
)
