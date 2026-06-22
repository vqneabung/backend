package com.vqn.bizflow.backend.inventory.service

import com.vqn.bizflow.backend.dto.PaginationResponse
import com.vqn.bizflow.backend.inventory.entity.InventoryHistoryEntity
import com.vqn.bizflow.backend.inventory.entity.MovementType
import com.vqn.bizflow.backend.inventory.entity.RefType
import com.vqn.bizflow.backend.inventory.repository.InventoryHistoryRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class InventoryHistoryResponse(
    val id: UUID,
    val productId: UUID,
    val movementType: String,
    val quantity: BigDecimal,
    val balanceAfter: BigDecimal,
    val refType: String,
    val refId: UUID,
    val referenceNumber: String?,
    val createdAt: Instant,
)

@Service
@Transactional(readOnly = true)
class InventoryHistoryService(
    private val historyRepo: InventoryHistoryRepository,
) {
    @Transactional
    fun log(
        ownerId: UUID,
        productId: UUID,
        movementType: MovementType,
        quantity: BigDecimal,
        balanceAfter: BigDecimal,
        refType: RefType,
        refId: UUID,
        referenceNumber: String?,
    ) {
        historyRepo.save(
            InventoryHistoryEntity(
                ownerId = ownerId,
                productId = productId,
                movementType = movementType,
                quantity = quantity,
                balanceAfter = balanceAfter,
                refType = refType,
                refId = refId,
                referenceNumber = referenceNumber,
            )
        )
    }

    fun listByProduct(ownerId: UUID, productId: UUID, page: Int, size: Int): PaginationResponse<InventoryHistoryResponse> {
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), size.coerceAtLeast(1))
        val result = historyRepo.findByOwnerIdAndProductIdOrderByCreatedAtDesc(ownerId, productId, pageable)
        val items = result.content.map { e ->
            InventoryHistoryResponse(
                id = requireNotNull(e.id),
                productId = e.productId,
                movementType = e.movementType.name,
                quantity = e.quantity,
                balanceAfter = e.balanceAfter,
                refType = e.refType.name,
                refId = e.refId,
                referenceNumber = e.referenceNumber,
                createdAt = e.createdAt,
            )
        }
        return PaginationResponse.of(items, page, size, result.totalElements)
    }
}
