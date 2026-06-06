package com.vqn.bizflow.backend.product.repository

import com.vqn.bizflow.backend.product.entity.UnitEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

/**
 * Repository cho UnitEntity.
 *
 * Khi list units cho 1 user: trả về global units (owner_id IS NULL)
 * + user-defined units của user đó.
 */
interface UnitRepository : JpaRepository<UnitEntity, UUID> {

    /** Lấy danh sách units: global + user-defined (theo ownerId) */
    @Query("""
        SELECT u FROM UnitEntity u 
        WHERE u.ownerId IS NULL OR u.ownerId = :ownerId
        ORDER BY u.ownerId NULLS FIRST, u.name ASC
    """)
    fun findByOwnerIdOrGlobal(@Param("ownerId") ownerId: UUID): List<UnitEntity>

    /** Kiểm tra trùng tên (trong cùng scope) */
    @Query("""
        SELECT COUNT(u) > 0 FROM UnitEntity u 
        WHERE u.name = :name AND (u.ownerId IS NULL OR u.ownerId = :ownerId)
    """)
    fun existsByNameForOwner(@Param("name") name: String, @Param("ownerId") ownerId: UUID): Boolean

    /** Tìm unit theo tên (trong scope của owner) */
    @Query("""
        SELECT u FROM UnitEntity u 
        WHERE u.name = :name AND (u.ownerId IS NULL OR u.ownerId = :ownerId)
    """)
    fun findByNameForOwner(@Param("name") name: String, @Param("ownerId") ownerId: UUID): UnitEntity?
}
