package com.vqn.bizflow.backend.customer.mapper

import com.vqn.bizflow.backend.customer.dto.CustomerResponse
import com.vqn.bizflow.backend.customer.entity.CustomerEntity
import org.mapstruct.Mapper

/**
 * MapStruct mapper: chuyển đổi giữa CustomerEntity và CustomerResponse.
 *
 * Tất cả fields trùng tên được MapStruct tự động map.
 * Các field chỉ có ở entity (ownerId, version, isActive) được tự động bỏ qua.
 */
@Mapper(componentModel = "spring")
interface CustomerMapper {

    /** Chuyển Entity → Response */
    fun toResponse(entity: CustomerEntity): CustomerResponse
}
