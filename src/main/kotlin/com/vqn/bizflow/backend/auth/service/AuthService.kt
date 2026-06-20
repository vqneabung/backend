package com.vqn.bizflow.backend.auth.service

import com.vqn.bizflow.backend.audit.service.AuditService
import com.vqn.bizflow.backend.auth.dto.AuthResponse
import com.vqn.bizflow.backend.auth.dto.LoginRequest
import com.vqn.bizflow.backend.auth.dto.RegisterRequest
import com.vqn.bizflow.backend.auth.dto.UserResponse
import com.vqn.bizflow.backend.auth.dto.UserUpdateRequest
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.UUID
import com.vqn.bizflow.backend.auth.entity.Role
import com.vqn.bizflow.backend.auth.entity.UserEntity
import com.vqn.bizflow.backend.auth.repository.UserRepository
import com.vqn.bizflow.backend.exception.BadRequestException
import com.vqn.bizflow.backend.exception.DuplicateException
import com.vqn.bizflow.backend.exception.UnauthorizedException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * AuthService — Xử lý đăng ký, đăng nhập và lấy thông tin user.
 *
 * Error messages lấy từ MessageSource (có i18n) thay vì hardcode.
 * Locale được detect từ Accept-Language header (LocaleConfig).
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val messageSource: MessageSource,
    private val auditService: AuditService,
) {
    /** Lấy thông tin user hiện tại từ userId (UUID). */
    fun me(userId: UUID): UserResponse {
        val user = userRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        }
        return UserResponse(
            id = requireNotNull(user.id) { "User ID must not be null after DB load" },
            email = user.email,
            name = user.name,
            role = user.role.name,
            joinedAt = user.createdAt,
            emailVerifiedAt = user.emailVerifiedAt,
            isActive = user.isActive,
        )
    }

    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateException(msg("auth.register.duplicate"))
        }

        val role = try {
            request.role?.let { Role.valueOf(it.uppercase()) } ?: Role.USER
        } catch (e: IllegalArgumentException) {
            throw BadRequestException(
                "${msg("auth.register.invalid-role")}: ${request.role}"
            )
        }

        val hashedPassword = requireNotNull(passwordEncoder.encode(request.password)) {
            "BCryptPasswordEncoder.encode() must not return null"
        }
        val user = UserEntity(
            email = request.email,
            password = hashedPassword,
            role = role,
            name = request.name
        )

        val savedUser = userRepository.save(user)
        val savedUserId = requireNotNull(savedUser.id) { "User ID must not be null after save" }

        val token = jwtService.generateToken(
            userId = savedUserId,
            email = savedUser.email,
            role = savedUser.role.name
        )

        return AuthResponse(
            token = token,
            email = savedUser.email,
            role = savedUser.role.name,
            name = savedUser.name,
            id = savedUserId,
            joinedAt = savedUser.createdAt,
        ).also {
            auditService.log(
                ownerId = savedUserId,
                actorId = savedUserId,
                action = "CREATE",
                entityType = "User",
                entityId = savedUserId,
                snapshot = "{\"email\": \"${savedUser.email.replace("\"", "\\\"")}\"}",
            )
        }
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw UnauthorizedException(msg("auth.login.invalid"))

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw UnauthorizedException(msg("auth.login.invalid"))
        }

        val token = jwtService.generateToken(
            userId = requireNotNull(user.id) { "User ID must not be null after DB load" },
            email = user.email,
            role = user.role.name
        )

        return AuthResponse(
            token = token,
            email = user.email,
            role = user.role.name,
            name = user.name,
            id = user.id,
            joinedAt = user.createdAt,
        )
    }

    fun updateProfile(userId: UUID, request: UserUpdateRequest): UserResponse {
        val user = userRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        }

        request.name?.let { user.name = it }

        val savedUser = userRepository.save(user)

        return UserResponse(
            id = requireNotNull(savedUser.id) { "User ID must not be null after save" },
            email = savedUser.email,
            name = savedUser.name,
            role = savedUser.role.name,
            joinedAt = savedUser.createdAt,
            emailVerifiedAt = savedUser.emailVerifiedAt,
            isActive = savedUser.isActive,
        )
    }

    /** Helper — lấy i18n message, không dùng array args (tránh Kotlin/Java interop issue) */
    private fun msg(code: String): String {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale())
    }
}
