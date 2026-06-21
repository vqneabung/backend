package com.vqn.bizflow.backend.auth.repository

import com.vqn.bizflow.backend.auth.entity.Role
import com.vqn.bizflow.backend.auth.entity.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean

    @Query("""
        SELECT u FROM UserEntity u
        WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    fun searchByEmailOrName(search: String, pageable: Pageable): Page<UserEntity>

    fun findByOwnerIdAndRole(ownerId: UUID, role: Role, pageable: Pageable): Page<UserEntity>
}
