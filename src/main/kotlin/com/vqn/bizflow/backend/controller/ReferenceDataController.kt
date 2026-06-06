package com.vqn.bizflow.backend.controller

import com.vqn.bizflow.backend.dto.ApiResponse
import com.vqn.bizflow.backend.product.dto.CategoryResponse
import com.vqn.bizflow.backend.product.dto.UnitResponse
import com.vqn.bizflow.backend.product.service.ReferenceDataService
import com.vqn.bizflow.backend.util.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

/**
 * ReferenceDataController — API cho danh mục và đơn vị tính.
 *
 * Các dropdown trên form product:
 * - GET /api/units → list units (global + user-defined)
 * - GET /api/categories → list categories (global + user-defined)
 * - POST /api/units → tạo unit mới (user-defined)
 * - POST /api/categories → tạo category mới (user-defined)
 */
@Tag(name = "Reference Data", description = "Danh mục & đơn vị tính")
@RestController
@RequestMapping("/api")
class ReferenceDataController(
    private val referenceDataService: ReferenceDataService,
) {
    // ===== Units =====

    @Operation(summary = "Danh sách đơn vị tính", description = "Global + user-defined (theo user đang đăng nhập)")
    @GetMapping("/units")
    fun listUnits(auth: Authentication): ResponseEntity<List<UnitResponse>> {
        val result = referenceDataService.listUnits(SecurityUtils.getUserId(auth))
        return ResponseEntity.ok(result)
    }

    @Operation(summary = "Tạo đơn vị tính mới", description = "Tạo unit user-defined cho user hiện tại")
    @PostMapping("/units")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun createUnit(
        auth: Authentication,
        @RequestParam name: String,
        @RequestParam(required = false) description: String?,
    ): ResponseEntity<ApiResponse<UnitResponse>> {
        val result = referenceDataService.createUnit(SecurityUtils.getUserId(auth), name, description)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(result, "Unit created"))
    }

    @Operation(summary = "Find-or-create unit", description = "Nếu đã tồn tại (global hoặc user), trả về. Chưa có thì tạo mới.")
    @PostMapping("/units/find-or-create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun findOrCreateUnit(
        auth: Authentication,
        @RequestParam name: String,
        @RequestParam(required = false) description: String?,
    ): ResponseEntity<ApiResponse<UnitResponse>> {
        val result = referenceDataService.findOrCreateUnit(SecurityUtils.getUserId(auth), name, description)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    // ===== Categories =====

    @Operation(summary = "Danh sách danh mục", description = "Global + user-defined (theo user đang đăng nhập)")
    @GetMapping("/categories")
    fun listCategories(auth: Authentication): ResponseEntity<List<CategoryResponse>> {
        val result = referenceDataService.listCategories(SecurityUtils.getUserId(auth))
        return ResponseEntity.ok(result)
    }

    @Operation(summary = "Tạo danh mục mới", description = "Tạo category user-defined cho user hiện tại")
    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun createCategory(
        auth: Authentication,
        @RequestParam name: String,
        @RequestParam(required = false) description: String?,
    ): ResponseEntity<ApiResponse<CategoryResponse>> {
        val result = referenceDataService.createCategory(SecurityUtils.getUserId(auth), name, description)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(result, "Category created"))
    }

    @Operation(summary = "Find-or-create category", description = "Nếu đã tồn tại (global hoặc user), trả về. Chưa có thì tạo mới.")
    @PostMapping("/categories/find-or-create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun findOrCreateCategory(
        auth: Authentication,
        @RequestParam name: String,
        @RequestParam(required = false) description: String?,
    ): ResponseEntity<ApiResponse<CategoryResponse>> {
        val result = referenceDataService.findOrCreateCategory(SecurityUtils.getUserId(auth), name, description)
        return ResponseEntity.ok(ApiResponse.success(result))
    }
}
