package com.vqn.bizflow.backend.product.repository

import com.vqn.bizflow.backend.product.entity.ProductUnitEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProductUnitRepository : JpaRepository<ProductUnitEntity, UUID> {
    fun findByProductId(productId: UUID): List<ProductUnitEntity>
    fun existsByProductIdAndUnit(productId: UUID, unit: String): Boolean
    fun countByProductId(productId: UUID): Long
}
