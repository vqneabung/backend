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
 * - categoryName: mapped trực tiếp từ entity (computed via @Formula)
 * - primaryUnitName: mapped trực tiếp từ entity (computed via @Formula)
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
     * - categoryName: từ entity.categoryName (@Formula)
     * - primaryUnitName: từ entity.primaryUnitName (@Formula)
     * - isLowStock: computed: stock < minStock.
     * - imageKeys: sorted theo position ASC, lấy objectKey.
     */
    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "categoryName", source = "categoryName")
    @Mapping(target = "primaryUnitName", source = "primaryUnitName")
    @Mapping(target = "isLowStock", expression = "java(entity.getStock().compareTo(entity.getMinStock()) < 0)")
    @Mapping(
        target = "imageKeys",
        expression = "java(entity.getImages() != null ? entity.getImages().stream().sorted(java.util.Comparator.comparingInt(com.vqn.bizflow.backend.product.entity.ProductImageEntity::getPosition)).map(com.vqn.bizflow.backend.product.entity.ProductImageEntity::getObjectKey).toList() : java.util.Collections.emptyList())",
    )
    fun toResponse(entity: ProductEntity): ProductResponse
}
