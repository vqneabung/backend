package com.vqn.bizflow.backend.audit.dto

import com.vqn.bizflow.backend.audit.entity.AuditLogEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

@Schema(description = "Audit log entry response")
data class AuditLogResponse(
    @field:Schema(description = "Audit log ID")
    val id: UUID,

    @field:Schema(description = "Action performed", example = "CREATE")
    val action: String,

    @field:Schema(description = "Entity type", example = "Product")
    val entityType: String,

    @field:Schema(description = "Target entity ID")
    val entityId: UUID,

    @field:Schema(description = "Actor ID who performed the action")
    val actorId: UUID?,

    @field:Schema(description = "Optional JSON snapshot of key fields")
    val entitySnapshot: String?,

    @field:Schema(description = "When the action was logged")
    val createdAt: Instant,
) {
    companion object {
        fun from(entity: AuditLogEntity): AuditLogResponse =
            AuditLogResponse(
                id = requireNotNull(entity.id) { "Audit log ID must not be null" },
                action = entity.action,
                entityType = entity.entityType,
                entityId = entity.entityId,
                actorId = entity.actorId,
                entitySnapshot = entity.entitySnapshot,
                createdAt = entity.createdAt,
            )
    }
}
