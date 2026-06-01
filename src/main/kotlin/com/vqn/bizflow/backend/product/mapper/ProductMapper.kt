package com.vqn.bizflow.backend.product.mapper

import com.vqn.bizflow.backend.product.dto.ProductResponse
import com.vqn.bizflow.backend.product.entity.ProductEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping

/**
 * MapStruct mapper: chuyển đổi giữa ProductEntity và ProductResponse.
 *
 * Tự động map các field trùng tên:
 * - Entity.name → Response.name
 * - Entity.price → Response.price
 * - Entity.stock → Response.stock
 * - ...
 *
 * Field đặc biệt:
 * - isLowStock: computed từ stock < minStock (dùng expression)
 *
 * Các field chỉ có ở entity (ownerId, version) được MapStruct tự động bỏ qua.
 */
@Mapper(componentModel = "spring")
interface ProductMapper {

    /**
     * Chuyển Entity → Response.
     *
     * Tự động map các field trùng tên (name, price, stock...).
     * isLowStock là computed: stock < minStock.
     * isActive: MapStruct báo unmapped warning do Kotlin "is" prefix convention,
     * nhưng mapper vẫn hoạt động vì constructor matching.
     */
    @Mapping(target = "isLowStock", expression = "java(entity.getStock().compareTo(entity.getMinStock()) < 0)")
    fun toResponse(entity: ProductEntity): ProductResponse

    /** Chuyển danh sách Entity → danh sách Response. */
    fun toResponseList(entities: List<ProductEntity>): List<ProductResponse>
}
