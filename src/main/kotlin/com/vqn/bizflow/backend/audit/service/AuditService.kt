package com.vqn.bizflow.backend.audit.service

import com.vqn.bizflow.backend.audit.dto.AuditLogResponse
import com.vqn.bizflow.backend.audit.entity.AuditLogEntity
import com.vqn.bizflow.backend.audit.repository.AuditLogRepository
import com.vqn.bizflow.backend.dto.PaginationResponse
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AuditService(private val auditLogRepo: AuditLogRepository) {
    private val log = LoggerFactory.getLogger(AuditService::class.java)

    companion object {
        private const val DEFAULT_PAGE = 1
        private const val DEFAULT_SIZE = 20
        private const val MAX_SIZE = 100
    }

    /**
     * Ghi lại thao tác CUD.
     * Bắt lỗi để audit failure không làm fail operation chính.
     */
    fun log(
        ownerId: UUID,
        actorId: UUID?,
        action: String,
        entityType: String,
        entityId: UUID,
        snapshot: String? = null,
    ) {
        try {
            auditLogRepo.save(
                AuditLogEntity(
                    ownerId = ownerId,
                    actorId = actorId,
                    action = action,
                    entityType = entityType,
                    entityId = entityId,
                    entitySnapshot = snapshot,
                )
            )
        } catch (e: Exception) {
            log.warn("Failed to write audit log for {} {} (action={}): {}", entityType, entityId, action, e.message)
        }
    }

    @Transactional(readOnly = true)
    fun list(
        ownerId: UUID,
        page: Int,
        size: Int,
        entityType: String? = null,
    ): PaginationResponse<AuditLogResponse> {
        val p = page.coerceAtLeast(DEFAULT_PAGE)
        val s = size.coerceIn(1, MAX_SIZE)
        val pageable = PageRequest.of(p - 1, s, Sort.by(Sort.Direction.DESC, "createdAt"))

        val result = if (entityType.isNullOrBlank()) {
            auditLogRepo.findByOwnerIdOrderByCreatedAtDesc(ownerId, pageable)
        } else {
            auditLogRepo.findByOwnerIdAndEntityTypeOrderByCreatedAtDesc(ownerId, entityType, pageable)
        }

        return PaginationResponse.of(
            data = result.content.map(AuditLogResponse::from),
            page = p,
            size = s,
            totalElements = result.totalElements,
        )
    }
}
