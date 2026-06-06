package com.vqn.bizflow.backend.product.repository

import com.vqn.bizflow.backend.product.entity.ProductUnitEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ProductUnitRepository : JpaRepository<ProductUnitEntity, UUID> {
    fun findByProductId(productId: UUID): List<ProductUnitEntity>
    fun existsByProductIdAndUnitId(productId: UUID, unitId: UUID): Boolean
    fun countByProductId(productId: UUID): Long

    @Query("""
        SELECT COUNT(pu) > 0 FROM ProductUnitEntity pu 
        WHERE pu.productId = :productId AND pu.unitId = :unitId
    """)
    fun existsByProductIdAndUnitIdCustom(@Param("productId") productId: UUID, @Param("unitId") unitId: UUID): Boolean
}
