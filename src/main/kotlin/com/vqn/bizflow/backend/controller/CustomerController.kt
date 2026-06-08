package com.vqn.bizflow.backend.controller

import com.vqn.bizflow.backend.customer.dto.CreateCustomerRequest
import com.vqn.bizflow.backend.customer.dto.CustomerResponse
import com.vqn.bizflow.backend.customer.dto.UpdateCustomerRequest
import com.vqn.bizflow.backend.customer.service.CustomerService
import com.vqn.bizflow.backend.dto.ApiResponse
import com.vqn.bizflow.backend.dto.PaginationResponse
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
 * CustomerController — REST API quản lý khách hàng.
 *
 * Phân quyền:
 * - Tất cả endpoints yêu cầu authentication (JWT)
 * - CREATE/UPDATE/DELETE: yêu cầu role USER hoặc ADMIN
 * - READ: tất cả roles
 * Multi-tenant: mỗi owner chỉ thấy customer của mình.
 */
@Tag(name = "Customers", description = "Quản lý khách hàng")
@RestController
@RequestMapping("/api/customers")
class CustomerController(
    private val customerService: CustomerService,
) {
    @Operation(
        summary = "Tạo khách hàng mới",
        description = "USER và ADMIN được tạo khách hàng. Kiểm tra trùng tên.",
    )
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "201", description = "Tạo thành công"),
        SwaggerApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        SwaggerApiResponse(responseCode = "409", description = "Tên khách hàng đã tồn tại"),
    ])
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun create(
        auth: Authentication,
        @Valid @RequestBody request: CreateCustomerRequest,
    ): ResponseEntity<ApiResponse<CustomerResponse>> {
        val response = customerService.create(SecurityUtils.getUserId(auth), request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(response, "Customer created"))
    }

    @Operation(
        summary = "Danh sách khách hàng",
        description = "Phân trang, tìm kiếm theo tên. Tất cả roles đều xem được.",
    )
    @GetMapping
    fun list(
        auth: Authentication,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<PaginationResponse<CustomerResponse>> {
        val result = customerService.list(
            SecurityUtils.getUserId(auth), search, page, size,
        )
        return ResponseEntity.ok(result)
    }

    @Operation(summary = "Chi tiết khách hàng")
    @GetMapping("/{id}")
    fun getById(
        auth: Authentication,
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<CustomerResponse>> {
        val result = customerService.getById(SecurityUtils.getUserId(auth), id)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @Operation(
        summary = "Cập nhật khách hàng",
        description = "USER và ADMIN được sửa. Tất cả fields optional (PATCH-style).",
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun update(
        auth: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateCustomerRequest,
    ): ResponseEntity<ApiResponse<CustomerResponse>> {
        val result = customerService.update(SecurityUtils.getUserId(auth), id, request)
        return ResponseEntity.ok(ApiResponse.success(result, "Customer updated"))
    }

    @Operation(
        summary = "Ẩn khách hàng (soft delete)",
        description = "USER và ADMIN. Không xóa vật lý.",
    )
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun deactivate(
        auth: Authentication,
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Unit>> {
        customerService.deactivate(SecurityUtils.getUserId(auth), id)
        return ResponseEntity.ok(ApiResponse.ok("Customer deactivated"))
    }
}
