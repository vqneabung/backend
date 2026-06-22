package com.vqn.bizflow.backend.inventory.repository

import com.vqn.bizflow.backend.inventory.entity.InventoryHistoryEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface InventoryHistoryRepository : JpaRepository<InventoryHistoryEntity, UUID> {
    fun findByOwnerIdAndProductIdOrderByCreatedAtDesc(
        ownerId: UUID, productId: UUID, pageable: Pageable
    ): Page<InventoryHistoryEntity>
}
