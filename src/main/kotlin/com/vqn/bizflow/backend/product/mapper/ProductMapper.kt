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
 * - categoryName: từ entity.category.name (@ManyToOne lazy)
 * - primaryUnitName: từ entity.primaryUnit.name (@ManyToOne lazy)
 * - isLowStock: computed từ stock < minStock (dùng expression)
 * - imageKeys: lấy từ entity.images theo position ASC (expression)
 *
 * Các field chỉ có ở entity (ownerId, version) được MapStruct tự động bỏ qua.
 */
@Mapper(componentModel = "spring")
interface ProductMapper {

    /**
     * Chuyển Entity → Response.
     *
     * - categoryId: từ entity.category?.id (@ManyToOne)
     * - primaryUnitId: từ entity.primaryUnit?.id (@ManyToOne)
     * - categoryName: từ entity.category?.name (@ManyToOne)
     * - primaryUnitName: từ entity.primaryUnit?.name (@ManyToOne)
     * - isLowStock: computed: stock < minStock.
     * - imageKeys: sorted theo position ASC, lấy objectKey.
     */
    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "categoryId", expression = "java(entity.getCategory() != null ? entity.getCategory().getId() : null)")
    @Mapping(target = "categoryName", expression = "java(entity.getCategory() != null ? entity.getCategory().getName() : null)")
    @Mapping(target = "primaryUnitId", expression = "java(entity.getPrimaryUnit().getId())")
    @Mapping(target = "primaryUnitName", expression = "java(entity.getPrimaryUnit().getName())")
    @Mapping(target = "isLowStock", expression = "java(entity.getStock().compareTo(entity.getMinStock()) < 0)")
    @Mapping(
        target = "imageKeys",
        expression = "java(entity.getImages() != null ? entity.getImages().stream().sorted(java.util.Comparator.comparingInt(com.vqn.bizflow.backend.product.entity.ProductImageEntity::getPosition)).map(com.vqn.bizflow.backend.product.entity.ProductImageEntity::getObjectKey).toList() : java.util.Collections.emptyList())",
    )
    fun toResponse(entity: ProductEntity): ProductResponse
}
