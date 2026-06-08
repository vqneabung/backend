package com.vqn.bizflow.backend.inventory.mapper

import com.vqn.bizflow.backend.inventory.dto.StockImportItemResponse
import com.vqn.bizflow.backend.inventory.dto.StockImportResponse
import com.vqn.bizflow.backend.inventory.entity.StockImportEntity
import com.vqn.bizflow.backend.inventory.entity.StockImportItemEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping

/**
 * MapStruct mapper cho StockImport.
 *
 * items map riêng ở service (cần product name từ ProductRepository).
 */
@Mapper(componentModel = "spring")
abstract class StockImportMapper {

    /**
     * Map entity → response (items được set riêng sau).
     */
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "itemCount", ignore = true)
    abstract fun toResponse(entity: StockImportEntity): StockImportResponse

    /**
     * Map 1 item entity → response (cần product name từ service).
     */
    @Mapping(target = "productName", source = "productName")
    abstract fun toItemResponse(
        entity: StockImportItemEntity,
        productName: String,
    ): StockImportItemResponse

    /**
     * Build full response với items.
     */
    fun toDetailResponse(
        entity: StockImportEntity,
        items: List<StockImportItemResponse>,
    ): StockImportResponse {
        val resp = toResponse(entity)
        return resp.copy(
            items = items,
            itemCount = items.size,
        )
    }
}
