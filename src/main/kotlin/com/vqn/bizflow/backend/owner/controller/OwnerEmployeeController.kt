package com.vqn.bizflow.backend.owner.controller

import com.vqn.bizflow.backend.auth.dto.UserResponse
import com.vqn.bizflow.backend.dto.ApiResponse
import com.vqn.bizflow.backend.dto.PaginationResponse
import com.vqn.bizflow.backend.owner.dto.CreateEmployeeRequest
import com.vqn.bizflow.backend.owner.dto.ResetEmployeePasswordRequest
import com.vqn.bizflow.backend.owner.service.OwnerEmployeeService
import com.vqn.bizflow.backend.util.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * OwnerEmployeeController — API quản lý nhân viên (EMPLOYEE) cho Owner.
 *
 * Phân quyền:
 * - Chỉ Owner (role USER) mới truy cập được. Admin có endpoint riêng ở /api/admin/users.
 * - Employee có thể login nhưng không có endpoint tạo user khác.
 *
 * Endpoints:
 * - GET    /api/owner/employees                — danh sách (phân trang)
 * - GET    /api/owner/employees/{id}           — chi tiết
 * - POST   /api/owner/employees                — tạo employee mới (role = EMPLOYEE, ownerId = auth)
 * - PATCH  /api/owner/employees/{id}/deactivate — soft delete
 * - PATCH  /api/owner/employees/{id}/reset-password — đặt lại mật khẩu
 */
@Tag(name = "Owner - Employees", description = "Quản lý nhân viên (Owner only)")
@RestController
@RequestMapping("/api/owner/employees")
@PreAuthorize("hasRole('USER')")
class OwnerEmployeeController(
    private val ownerEmployeeService: OwnerEmployeeService,
) {
    @Operation(summary = "Danh sách nhân viên của Owner (phân trang)")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Thành công"),
        SwaggerApiResponse(responseCode = "403", description = "Không có quyền Owner"),
    ])
    @GetMapping
    fun list(
        auth: Authentication,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<PaginationResponse<UserResponse>> {
        val ownerId = SecurityUtils.getUserId(auth)
        val result = ownerEmployeeService.list(ownerId, page, size)
        return ResponseEntity.ok(result)
    }

    @Operation(summary = "Chi tiết nhân viên")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Thành công"),
        SwaggerApiResponse(responseCode = "403", description = "Không phải employee của Owner"),
        SwaggerApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên"),
    ])
    @GetMapping("/{id}")
    fun getById(
        auth: Authentication,
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val ownerId = SecurityUtils.getUserId(auth)
        val employee = ownerEmployeeService.getById(ownerId, id)
        return ResponseEntity.ok(ApiResponse.success(employee))
    }

    @Operation(summary = "Tạo nhân viên mới")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "201", description = "Tạo thành công"),
        SwaggerApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        SwaggerApiResponse(responseCode = "409", description = "Email đã tồn tại"),
    ])
    @PostMapping
    fun create(
        auth: Authentication,
        @Valid @RequestBody request: CreateEmployeeRequest,
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val ownerId = SecurityUtils.getUserId(auth)
        val employee = ownerEmployeeService.create(ownerId, request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(employee, "employee.created"))
    }

    @Operation(summary = "Vô hiệu hóa nhân viên (soft delete)")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Vô hiệu hóa thành công"),
        SwaggerApiResponse(responseCode = "403", description = "Không phải employee của Owner"),
        SwaggerApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên"),
    ])
    @PatchMapping("/{id}/deactivate")
    fun deactivate(
        auth: Authentication,
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Unit>> {
        val ownerId = SecurityUtils.getUserId(auth)
        ownerEmployeeService.deactivate(ownerId, id)
        return ResponseEntity.ok(ApiResponse.ok(message = "employee.deactivated"))
    }

    @Operation(summary = "Đặt lại mật khẩu nhân viên")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Đặt lại mật khẩu thành công"),
        SwaggerApiResponse(responseCode = "400", description = "Mật khẩu không hợp lệ"),
        SwaggerApiResponse(responseCode = "403", description = "Không phải employee của Owner"),
        SwaggerApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên"),
    ])
    @PatchMapping("/{id}/reset-password")
    fun resetPassword(
        auth: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody request: ResetEmployeePasswordRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
        val ownerId = SecurityUtils.getUserId(auth)
        ownerEmployeeService.resetPassword(ownerId, id, request)
        return ResponseEntity.ok(ApiResponse.ok(message = "employee.password-reset"))
    }
}
