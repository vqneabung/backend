package com.vqn.bizflow.backend.product.mapper

import com.vqn.bizflow.backend.product.dto.ProductResponse
import com.vqn.bizflow.backend.product.entity.ProductEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping

/**
 * MapStruct mapper: chuyển đổi giữa ProductEntity và ProductResponse.
 *
 * Tự động map các field trùng tên.
 * Field đặc biệt:
 * - categoryName: lấy từ categoryRef.name (read-only @ManyToOne)
 * - primaryUnitName: lấy từ primaryUnitRef.name
 * - isLowStock: computed từ stock < minStock (dùng expression)
 * - imageKeys: lấy từ entity.images theo position ASC (expression)
 *
 * Các field chỉ có ở entity (ownerId, version, categoryRef, primaryUnitRef)
 * được MapStruct tự động bỏ qua.
 */
@Mapper(componentModel = "spring")
interface ProductMapper {

    /**
     * Chuyển Entity → Response.
     *
     * - categoryName: từ categoryRef.name (nullable)
     * - primaryUnitName: từ primaryUnitRef.name
     * - isLowStock: computed: stock < minStock.
     * - imageKeys: sorted theo position ASC, lấy objectKey.
     */
    @Mapping(target = "categoryName", expression = "java(entity.getCategoryRef() != null ? entity.getCategoryRef().getName() : null)")
    @Mapping(target = "primaryUnitName", expression = "java(entity.getPrimaryUnitRef() != null ? entity.getPrimaryUnitRef().getName() : null)")
    @Mapping(target = "isLowStock", expression = "java(entity.getStock().compareTo(entity.getMinStock()) < 0)")
    @Mapping(
        target = "imageKeys",
        expression = "java(entity.getImages() != null ? entity.getImages().stream().sorted(java.util.Comparator.comparingInt(com.vqn.bizflow.backend.product.entity.ProductImageEntity::getPosition)).map(com.vqn.bizflow.backend.product.entity.ProductImageEntity::getObjectKey).toList() : java.util.Collections.emptyList())",
    )
    fun toResponse(entity: ProductEntity): ProductResponse
}
