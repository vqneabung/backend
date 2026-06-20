package com.vqn.bizflow.backend.admin.controller

import com.vqn.bizflow.backend.admin.dto.AdminUserUpdateRequest
import com.vqn.bizflow.backend.admin.service.AdminUserService
import com.vqn.bizflow.backend.auth.dto.UserResponse
import com.vqn.bizflow.backend.dto.ApiResponse
import com.vqn.bizflow.backend.dto.PaginationResponse
import com.vqn.bizflow.backend.util.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * AdminUserController — API quản lý users cho ADMIN role.
 *
 * Endpoints:
 * - GET    /api/admin/users          — danh sách (phân trang + search)
 * - GET    /api/admin/users/{id}     — chi tiết
 * - PUT    /api/admin/users/{id}     — cập nhật name + role
 * - DELETE /api/admin/users/{id}     — soft delete (set is_active = false)
 */
@Tag(name = "Admin - Users", description = "Quản lý users (ADMIN only)")
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
class AdminUserController(
    private val adminUserService: AdminUserService,
) {
    @Operation(summary = "Danh sách users (phân trang + search theo email/name)")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Thành công"),
        SwaggerApiResponse(responseCode = "403", description = "Không có quyền ADMIN"),
    ])
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
    ): ResponseEntity<PaginationResponse<UserResponse>> {
        val result = adminUserService.list(page, size, search)
        return ResponseEntity.ok(result)
    }

    @Operation(summary = "Chi tiết user theo id")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): ResponseEntity<ApiResponse<UserResponse>> {
        val user = adminUserService.getById(id)
        return ResponseEntity.ok(ApiResponse.success(user))
    }

    @Operation(summary = "Soft delete user (set is_active = false)")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Xóa thành công"),
        SwaggerApiResponse(responseCode = "404", description = "User không tồn tại"),
    ])
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        adminUserService.softDelete(id)
        return ResponseEntity.ok(ApiResponse.ok(message = "admin.user.deleted"))
    }

    @Operation(summary = "Cập nhật user (name + role)")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Cập nhật thành công"),
        SwaggerApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        SwaggerApiResponse(responseCode = "404", description = "User không tồn tại"),
    ])
    @PutMapping("/{id}")
    fun update(
        auth: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody request: AdminUserUpdateRequest,
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val response = adminUserService.update(id, request, SecurityUtils.getUserId(auth))
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}