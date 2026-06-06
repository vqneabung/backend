package com.vqn.bizflow.backend.product.repository

import com.vqn.bizflow.backend.product.entity.CategoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

/**
 * Repository cho CategoryEntity.
 *
 * Khi list categories cho 1 user: trả về global categories (owner_id IS NULL)
 * + user-defined categories của user đó.
 */
interface CategoryRepository : JpaRepository<CategoryEntity, UUID> {

    /** Lấy danh sách categories: global + user-defined (theo ownerId) */
    @Query("""
        SELECT c FROM CategoryEntity c 
        WHERE c.ownerId IS NULL OR c.ownerId = :ownerId
        ORDER BY c.ownerId NULLS FIRST, c.name ASC
    """)
    fun findByOwnerIdOrGlobal(@Param("ownerId") ownerId: UUID): List<CategoryEntity>

    /** Kiểm tra trùng tên (trong cùng scope) */
    @Query("""
        SELECT COUNT(c) > 0 FROM CategoryEntity c 
        WHERE c.name = :name AND (c.ownerId IS NULL OR c.ownerId = :ownerId)
    """)
    fun existsByNameForOwner(@Param("name") name: String, @Param("ownerId") ownerId: UUID): Boolean

    /** Tìm category theo tên (trong scope của owner) */
    @Query("""
        SELECT c FROM CategoryEntity c 
        WHERE c.name = :name AND (c.ownerId IS NULL OR c.ownerId = :ownerId)
    """)
    fun findByNameForOwner(@Param("name") name: String, @Param("ownerId") ownerId: UUID): CategoryEntity?
}
