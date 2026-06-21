package com.vqn.bizflow.backend.owner.service

import com.vqn.bizflow.backend.audit.service.AuditService
import com.vqn.bizflow.backend.auth.dto.UserResponse
import com.vqn.bizflow.backend.auth.entity.Role
import com.vqn.bizflow.backend.auth.entity.UserEntity
import com.vqn.bizflow.backend.auth.repository.UserRepository
import com.vqn.bizflow.backend.dto.PaginationResponse
import com.vqn.bizflow.backend.exception.DuplicateException
import com.vqn.bizflow.backend.exception.ForbiddenException
import com.vqn.bizflow.backend.exception.ResourceNotFoundException
import com.vqn.bizflow.backend.owner.dto.CreateEmployeeRequest
import com.vqn.bizflow.backend.owner.dto.ResetEmployeePasswordRequest
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * OwnerEmployeeService — Quản lý nhân viên (EMPLOYEE) cho Owner.
 *
 * Phân quyền:
 * - Chỉ Owner (role USER) mới dùng được service này (controller đã @PreAuthorize).
 * - Employee phải có ownerId trỏ về Owner hiện tại; nếu không khớp → 403.
 *
 * Multi-tenant isolation: mỗi Owner chỉ thấy/quản lý employees của mình.
 */
@Service
@Transactional
class OwnerEmployeeService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
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
        isActive = user.isActive,
        ownerId = user.ownerId,
    )

    fun create(ownerId: UUID, request: CreateEmployeeRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateException(msg("employee.email-duplicate"))
        }

        val hashedPassword = requireNotNull(passwordEncoder.encode(request.password)) {
            "BCryptPasswordEncoder.encode() must not return null"
        }

        val employee = UserEntity(
            email = request.email,
            password = hashedPassword,
            role = Role.EMPLOYEE,
            name = request.name,
            ownerId = ownerId,
        )

        val saved = userRepository.save(employee)
        val savedId = requireNotNull(saved.id) { "User ID must not be null after save" }

        auditService.log(
            ownerId = ownerId,
            actorId = ownerId,
            action = "CREATE",
            entityType = "Employee",
            entityId = savedId,
            snapshot = "{\"email\": \"${saved.email.replace("\"", "\\\"")}\"}",
        )

        return toResponse(saved)
    }

    @Transactional(readOnly = true)
    fun list(ownerId: UUID, page: Int?, size: Int?): PaginationResponse<UserResponse> {
        val effectivePage = (page ?: DEFAULT_PAGE).coerceAtLeast(1)
        val effectiveSize = (size ?: DEFAULT_SIZE).coerceIn(1, MAX_SIZE)
        val pageable = PageRequest.of(effectivePage - 1, effectiveSize, Sort.by("createdAt").descending())

        val resultPage = userRepository.findByOwnerIdAndRole(ownerId, Role.EMPLOYEE, pageable)

        return PaginationResponse.of(
            data = resultPage.content.map(::toResponse),
            page = effectivePage,
            size = effectiveSize,
            totalElements = resultPage.totalElements,
        )
    }

    @Transactional(readOnly = true)
    fun getById(ownerId: UUID, employeeId: UUID): UserResponse {
        val employee = findOwnedEmployee(ownerId, employeeId)
        return toResponse(employee)
    }

    fun deactivate(ownerId: UUID, employeeId: UUID) {
        val employee = findOwnedEmployee(ownerId, employeeId)
        employee.isActive = false
        userRepository.save(employee)
        auditService.log(
            ownerId = ownerId,
            actorId = ownerId,
            action = "DEACTIVATE",
            entityType = "Employee",
            entityId = employeeId,
            snapshot = "{\"email\": \"${employee.email.replace("\"", "\\\"")}\"}",
        )
    }

    fun resetPassword(ownerId: UUID, employeeId: UUID, request: ResetEmployeePasswordRequest) {
        val employee = findOwnedEmployee(ownerId, employeeId)
        employee.password = requireNotNull(passwordEncoder.encode(request.newPassword)) {
            "BCryptPasswordEncoder.encode() must not return null"
        }
        userRepository.save(employee)
        auditService.log(
            ownerId = ownerId,
            actorId = ownerId,
            action = "RESET_PASSWORD",
            entityType = "Employee",
            entityId = employeeId,
            snapshot = null,
        )
    }

    /**
     * Tìm employee theo id và verify ownership.
     * Throw 404 nếu không tồn tại, 403 nếu không thuộc Owner.
     */
    private fun findOwnedEmployee(ownerId: UUID, employeeId: UUID): UserEntity {
        val employee = userRepository.findById(employeeId)
            .orElseThrow { ResourceNotFoundException(msg("employee.not-found")) }
        if (employee.ownerId != ownerId) {
            throw ForbiddenException(msg("employee.not-owned"))
        }
        return employee
    }
}
