package com.vqn.bizflow.backend.config

import com.vqn.bizflow.backend.auth.entity.Role
import com.vqn.bizflow.backend.auth.entity.UserEntity
import com.vqn.bizflow.backend.auth.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * UserSeedService — Tạo users mặc định trong transaction riêng.
 *
 * Dùng @Transactional + repository.save().
 * BaseEntity implements Persistable<UUID> với @Transient isNew() flag
 * → save() gọi persist() cho entity mới (không gây StaleObjectStateException).
 */
@Service
class UserSeedService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    private val log = LoggerFactory.getLogger(UserSeedService::class.java)

    @Transactional
    fun seedIfEmpty() {
        if (userRepository.findByEmail("owner@bizflow.vn") != null) {
            log.info("Default users already exist, skipping seed")
            return
        }

        val defaultPassword = passwordEncoder.encode("123456")!!

        userRepository.save(UserEntity(
            email = "owner@bizflow.vn",
            password = defaultPassword,
            role = Role.USER,
            name = "Chủ cửa hàng"
        ))

        userRepository.save(UserEntity(
            email = "admin@bizflow.vn",
            password = defaultPassword,
            role = Role.ADMIN,
            name = "Quản trị viên"
        ))

        log.info("✅ Created default users: owner@bizflow.vn, admin@bizflow.vn")
    }
}
