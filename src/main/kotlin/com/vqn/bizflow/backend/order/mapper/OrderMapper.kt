package com.vqn.bizflow.backend.order.mapper

import com.vqn.bizflow.backend.order.dto.OrderItemResponse
import com.vqn.bizflow.backend.order.dto.OrderResponse
import com.vqn.bizflow.backend.order.entity.OrderEntity
import com.vqn.bizflow.backend.order.entity.OrderItemEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping

/**
 * MapStruct mapper cho Order.
 *
 * items map riêng ở service (cần product name từ ProductRepository).
 */
@Mapper(componentModel = "spring")
abstract class OrderMapper {

    /**
     * Map entity → response (items được set riêng sau).
     */
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "itemCount", ignore = true)
    abstract fun toResponse(entity: OrderEntity): OrderResponse

    /**
     * Map 1 item entity → response.
     */
    abstract fun toItemResponse(entity: OrderItemEntity): OrderItemResponse

    /**
     * Build full response với items.
     */
    fun toDetailResponse(
        entity: OrderEntity,
        items: List<OrderItemResponse>,
    ): OrderResponse {
        val resp = toResponse(entity)
        return resp.copy(
            items = items,
            itemCount = items.size,
        )
    }
}
