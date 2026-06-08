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
 * Tất cả query đều filter theo parent product — không bao giờ truy vấn toàn bộ.
 * Dùng `i.product.id` (bidirectional @ManyToOne) trong JPQL.
 */
interface ProductImageRepository : JpaRepository<ProductImageEntity, UUID> {

    /** Lấy tất cả ảnh của 1 product, sắp xếp theo position ASC */
    @Query("SELECT i FROM ProductImageEntity i WHERE i.product.id = :productId ORDER BY i.position ASC")
    fun findImagesByProductId(@Param("productId") productId: UUID): List<ProductImageEntity>

    /** Xóa tất cả ảnh của 1 product — dùng khi replace toàn bộ ảnh */
    @Modifying
    @Query("DELETE FROM ProductImageEntity i WHERE i.product.id = :productId")
    fun deleteByProductId(@Param("productId") productId: UUID)
}
