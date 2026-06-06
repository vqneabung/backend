package com.vqn.bizflow.backend.product.service

import com.vqn.bizflow.backend.exception.BadRequestException
import com.vqn.bizflow.backend.exception.DuplicateException
import com.vqn.bizflow.backend.exception.ResourceNotFoundException
import com.vqn.bizflow.backend.product.dto.CategoryResponse
import com.vqn.bizflow.backend.product.dto.UnitResponse
import com.vqn.bizflow.backend.product.entity.CategoryEntity
import com.vqn.bizflow.backend.product.entity.UnitEntity
import com.vqn.bizflow.backend.product.repository.CategoryRepository
import com.vqn.bizflow.backend.product.repository.UnitRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * ReferenceDataService — Quản lý danh mục và đơn vị tính.
 *
 * Cả Category và Unit đều có 2 loại:
 * - Global: ownerId = null (mọi user đều thấy)
 * - User-defined: ownerId = userId (riêng user đó)
 */
@Service
@Transactional
class ReferenceDataService(
    private val unitRepo: UnitRepository,
    private val categoryRepo: CategoryRepository,
) {

    // ===== Units =====

    @Transactional(readOnly = true)
    fun listUnits(userId: UUID): List<UnitResponse> {
        return unitRepo.findByOwnerIdOrGlobal(userId).map { it.toResponse() }
    }

    /** Thêm đơn vị tính mới (user-defined). */
    fun createUnit(userId: UUID, name: String, description: String?): UnitResponse {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) throw BadRequestException("Unit name is required")

        if (unitRepo.existsByNameForOwner(trimmedName, userId)) {
            throw DuplicateException("Unit '$trimmedName' already exists")
        }

        val entity = UnitEntity(ownerId = userId, name = trimmedName, description = description?.trim())
        return unitRepo.save(entity).toResponse()
    }

    /**
     * Find-or-create unit.
     * Dùng khi frontend muốn quick-create: gửi tên → tìm, nếu chưa có thì tạo mới.
     * Nếu global unit có tên đó, trả về global unit (không tạo duplicate).
     */
    fun findOrCreateUnit(userId: UUID, name: String, description: String?): UnitResponse {
        val trimmedName = name.trim()
        val existing = unitRepo.findByNameForOwner(trimmedName, userId)
        if (existing != null) return existing.toResponse()
        return createUnit(userId, trimmedName, description)
    }

    // ===== Categories =====

    @Transactional(readOnly = true)
    fun listCategories(userId: UUID): List<CategoryResponse> {
        return categoryRepo.findByOwnerIdOrGlobal(userId).map { it.toResponse() }
    }

    /** Thêm danh mục mới (user-defined). */
    fun createCategory(userId: UUID, name: String, description: String?): CategoryResponse {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) throw BadRequestException("Category name is required")

        if (categoryRepo.existsByNameForOwner(trimmedName, userId)) {
            throw DuplicateException("Category '$trimmedName' already exists")
        }

        val entity = CategoryEntity(ownerId = userId, name = trimmedName, description = description?.trim())
        return categoryRepo.save(entity).toResponse()
    }

    /** Find-or-create category. */
    fun findOrCreateCategory(userId: UUID, name: String, description: String?): CategoryResponse {
        val trimmedName = name.trim()
        val existing = categoryRepo.findByNameForOwner(trimmedName, userId)
        if (existing != null) return existing.toResponse()
        return createCategory(userId, trimmedName, description)
    }

    // ===== Resolve names (cho ProductService/ProductMapper) =====

    @Transactional(readOnly = true)
    fun getUnitName(unitId: UUID): String? {
        return unitRepo.findById(unitId).orElse(null)?.name
    }

    @Transactional(readOnly = true)
    fun getCategoryName(categoryId: UUID): String? {
        return categoryRepo.findById(categoryId).orElse(null)?.name
    }

    @Transactional(readOnly = true)
    fun getUnit(unitId: UUID): UnitEntity? {
        return unitRepo.findById(unitId).orElse(null)
    }

    @Transactional(readOnly = true)
    fun getCategory(categoryId: UUID): CategoryEntity? {
        return categoryRepo.findById(categoryId).orElse(null)
    }
}

// ===== Extension functions: Entity → Response =====

private fun UnitEntity.toResponse() = UnitResponse(
    id = id ?: throw IllegalStateException("Unit must be persisted"),
    name = name,
    description = description,
    ownerId = ownerId,
)

private fun CategoryEntity.toResponse() = CategoryResponse(
    id = id ?: throw IllegalStateException("Category must be persisted"),
    name = name,
    description = description,
    ownerId = ownerId,
)
