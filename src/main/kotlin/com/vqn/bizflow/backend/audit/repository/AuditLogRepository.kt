package com.vqn.bizflow.backend.audit.repository

import com.vqn.bizflow.backend.audit.entity.AuditLogEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AuditLogRepository : JpaRepository<AuditLogEntity, UUID> {
    fun findByOwnerIdOrderByCreatedAtDesc(ownerId: UUID, pageable: Pageable): Page<AuditLogEntity>

    fun findByOwnerIdAndEntityTypeOrderByCreatedAtDesc(
        ownerId: UUID,
        entityType: String,
        pageable: Pageable,
    ): Page<AuditLogEntity>
}
