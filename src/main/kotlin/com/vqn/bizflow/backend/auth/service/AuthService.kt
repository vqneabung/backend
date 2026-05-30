package com.vqn.bizflow.backend.auth.service

import com.vqn.bizflow.backend.auth.dto.AuthResponse
import com.vqn.bizflow.backend.auth.dto.LoginRequest
import com.vqn.bizflow.backend.auth.dto.RegisterRequest
import com.vqn.bizflow.backend.auth.entity.Role
import com.vqn.bizflow.backend.auth.entity.UserEntity
import com.vqn.bizflow.backend.auth.repository.UserRepository
import com.vqn.bizflow.backend.exception.BadRequestException
import com.vqn.bizflow.backend.exception.DuplicateException
import com.vqn.bizflow.backend.exception.UnauthorizedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateException("Email already exists")
        }

        val role = try {
            request.role?.let { Role.valueOf(it.uppercase()) } ?: Role.USER
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("Invalid role: ${request.role}")
        }

        val hashedPassword: String = passwordEncoder.encode(request.password)!!
        val user = UserEntity(
            email = request.email,
            password = hashedPassword,
            role = role,
            name = request.name
        )

        val savedUser = userRepository.save(user)

        val token = jwtService.generateToken(
            userId = savedUser.id,
            email = savedUser.email,
            role = savedUser.role.name
        )

        return AuthResponse(
            token = token,
            email = savedUser.email,
            role = savedUser.role.name,
            name = savedUser.name
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw UnauthorizedException("Invalid email or password")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw UnauthorizedException("Invalid email or password")
        }

        val token = jwtService.generateToken(
            userId = user.id,
            email = user.email,
            role = user.role.name
        )

        return AuthResponse(
            token = token,
            email = user.email,
            role = user.role.name,
            name = user.name
        )
    }
}