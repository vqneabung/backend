package com.vqn.bizflow.backend.admin.service

import com.vqn.bizflow.backend.audit.service.AuditService
import com.vqn.bizflow.backend.auth.dto.UserResponse
import com.vqn.bizflow.backend.auth.entity.UserEntity
import com.vqn.bizflow.backend.auth.repository.UserRepository
import com.vqn.bizflow.backend.dto.PaginationResponse
import com.vqn.bizflow.backend.exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * AdminUserService — Business logic cho admin user management.
 *
 * Soft delete: set is_active = false (giữ audit trail).
 * List/Get chỉ trả về active users (Hibernate @SQLRestriction tự filter).
 */
@Service
@Transactional
class AdminUserService(
    private val userRepository: UserRepository,
    private val messageSource: MessageSource,
    private val auditService: AuditService,
) {
    companion object {
        private const val DEFAULT_PAGE = 1
        private const val DEFAULT_SIZE = 20
        private const val MAX_SIZE = 100
    }

    private fun msg(code: String, vararg args: Any): String =
        messageSource.getMessage(code, args, LocaleContextHolder.getLocale())

    private fun toResponse(user: UserEntity) = UserResponse(
        id = requireNotNull(user.id) { "User ID must not be null" },
        email = user.email,
        name = user.name,
        role = user.role.name,
        joinedAt = user.createdAt,
        emailVerifiedAt = user.emailVerifiedAt,
    )

    @Transactional(readOnly = true)
    fun list(page: Int?, size: Int?, search: String?): PaginationResponse<UserResponse> {
        val effectivePage = (page ?: DEFAULT_PAGE).coerceAtLeast(1)
        val effectiveSize = (size ?: DEFAULT_SIZE).coerceIn(1, MAX_SIZE)
        val pageable = PageRequest.of(effectivePage - 1, effectiveSize, Sort.by("createdAt").descending())

        val resultPage = if (search.isNullOrBlank()) {
            userRepository.findAll(pageable)
        } else {
            userRepository.searchByEmailOrName(search.trim(), pageable)
        }

        return PaginationResponse.of(
            data = resultPage.content.map(::toResponse),
            page = effectivePage,
            size = effectiveSize,
            totalElements = resultPage.totalElements,
        )
    }

    @Transactional(readOnly = true)
    fun getById(id: UUID): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException(msg("user.not-found")) }
        return toResponse(user)
    }

    /** Soft delete: set is_active = false (giữ audit trail). */
    fun softDelete(id: UUID) {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException(msg("user.not-found")) }
        user.isActive = false
        userRepository.save(user)
        auditService.log(
            ownerId = id,
            actorId = id,
            action = "DEACTIVATE",
            entityType = "User",
            entityId = id,
            snapshot = "{\"email\": \"${user.email.replace("\"", "\\\"")}\"}",
        )
    }
}