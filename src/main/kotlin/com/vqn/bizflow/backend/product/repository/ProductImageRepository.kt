package com.vqn.bizflow.backend.product.repository

import com.vqn.bizflow.backend.product.entity.ProductImageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

/**
 * Repository cho ProductImageEntity.
 *
 * Tất cả query đều filter theo productId — không bao giờ truy vấn toàn bộ.
 */
interface ProductImageRepository : JpaRepository<ProductImageEntity, UUID> {

    /** Lấy tất cả ảnh của 1 product, sắp xếp theo position ASC */
    fun findByProductIdOrderByPositionAsc(productId: UUID): List<ProductImageEntity>

    /** Đếm số ảnh hiện tại của 1 product (validate max 5 trước khi thêm) */
    fun countByProductId(productId: UUID): Int

    /** Xóa tất cả ảnh của 1 product — dùng khi replace toàn bộ ảnh */
    @Modifying
    @Query("DELETE FROM ProductImageEntity i WHERE i.productId = :productId")
    fun deleteByProductId(@Param("productId") productId: UUID)
}
