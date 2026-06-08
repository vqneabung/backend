package com.vqn.bizflow.backend.customer.repository

import com.vqn.bizflow.backend.customer.entity.CustomerEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

/**
 * Repository cho CustomerEntity.
 *
 * @SQLRestriction(is_active = 1) tự động thêm vào mọi query →
 * không cần filter thủ công isActive.
 * Để include inactive records, dùng native query hoặc entityManager.clear().
 */
interface CustomerRepository : JpaRepository<CustomerEntity, UUID> {

    /** Danh sách customers của 1 owner, phân trang */
    @Query("SELECT c FROM CustomerEntity c WHERE c.ownerId = :ownerId")
    fun findByOwnerId(@Param("ownerId") ownerId: UUID, pageable: Pageable): Page<CustomerEntity>

    /** Tìm kiếm customer theo tên (LIKE) — cùng owner */
    @Query("""
        SELECT c FROM CustomerEntity c 
        WHERE c.ownerId = :ownerId 
        AND (:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    fun searchByOwnerId(
        @Param("ownerId") ownerId: UUID,
        @Param("search") search: String?,
        pageable: Pageable,
    ): Page<CustomerEntity>

    /** Kiểm tra trùng tên (trong cùng owner) */
    fun existsByNameAndOwnerId(name: String, ownerId: UUID): Boolean
}
