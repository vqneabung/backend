package com.vqn.bizflow.backend.auth.repository

import com.vqn.bizflow.backend.auth.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
}
