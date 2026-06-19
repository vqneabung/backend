package com.vqn.bizflow.backend.audit.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

/**
 * Audit log — ghi nhận thao tác CUD trên các entity nghiệp vụ.
 *
 * Không có relationship: chỉ lưu UUID reference để tránh side-effect
 * cascade và giữ table độc lập với lifecycle của entity chính.
 */
@Entity
@Table(name = "audit_logs")
class AuditLogEntity(
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "owner_id", nullable = false)
    val ownerId: UUID,

    @Column(name = "actor_id", nullable = true)
    val actorId: UUID? = null,

    @Column(name = "action", nullable = false, length = 50)
    val action: String,

    @Column(name = "entity_type", nullable = false, length = 50)
    val entityType: String,

    @Column(name = "entity_id", nullable = false)
    val entityId: UUID,

    @Column(name = "entity_snapshot", nullable = true)
    val entitySnapshot: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
)
