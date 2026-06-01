package com.vqn.bizflow.backend.controller

import com.vqn.bizflow.backend.dto.ApiResponse
import com.vqn.bizflow.backend.dto.PaginationResponse
import com.vqn.bizflow.backend.product.dto.CreateProductRequest
import com.vqn.bizflow.backend.product.dto.ProductResponse
import com.vqn.bizflow.backend.product.dto.UpdateProductRequest
import com.vqn.bizflow.backend.product.service.ProductService
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
import java.math.BigDecimal
import java.util.UUID

/**
 * ProductController — REST API quản lý sản phẩm.
 *
 * Phân quyền:
 * - Tất cả endpoints yêu cầu authentication (JWT)
 * - CREATE/UPDATE/DELETE: yêu cầu role OWNER
 * - READ: tất cả roles (USER, EMPLOYEE, OWNER, ADMIN)
 *
 * Response patterns:
 * - List: dùng PaginationResponse<ProductResponse> (generic, có pagination metadata)
 * - Single: dùng ApiResponse<ProductResponse> (success + message + data)
 * - Action (deactivate, addUnit, removeUnit): dùng ApiResponse<Unit>
 */
@Tag(name = "Products", description = "Quản lý sản phẩm")
@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService,
) {
    @Operation(
        summary = "Tạo sản phẩm mới",
        description = "Chỉ Owner mới được tạo sản phẩm. Kiểm tra trùng tên, barcode.",
    )
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "201", description = "Tạo thành công"),
        SwaggerApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        SwaggerApiResponse(responseCode = "409", description = "Tên sản phẩm đã tồn tại"),
    ])
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    fun create(
        auth: Authentication,
        @Valid @RequestBody request: CreateProductRequest,
    ): ResponseEntity<ApiResponse<ProductResponse>> {
        val response = productService.create(SecurityUtils.getUserId(auth), request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(response, "Product created"))
    }

    @Operation(
        summary = "Danh sách sản phẩm",
        description = "Phân trang, tìm kiếm, lọc theo category. Tất cả roles đều xem được.",
    )
    @GetMapping
    fun list(
        auth: Authentication,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) category: String?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sortBy: String?,
        @RequestParam(defaultValue = "desc") sortDir: String?,
    ): ResponseEntity<PaginationResponse<ProductResponse>> {
        val result = productService.list(
            SecurityUtils.getUserId(auth), search, category, page, size, sortBy, sortDir
        )
        return ResponseEntity.ok(result)
    }

    @Operation(summary = "Chi tiết sản phẩm")
    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<ProductResponse>> {
        val result = productService.getById(id)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @Operation(
        summary = "Cập nhật sản phẩm",
        description = "Chỉ Owner mới được sửa. Có optimistic locking (version).",
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    fun update(
        auth: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateProductRequest,
    ): ResponseEntity<ApiResponse<ProductResponse>> {
        val result = productService.update(SecurityUtils.getUserId(auth), id, request)
        return ResponseEntity.ok(ApiResponse.success(result, "Product updated"))
    }

    @Operation(
        summary = "Ẩn sản phẩm (soft delete)",
        description = "Chỉ Owner. Không xóa vật lý.",
    )
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('OWNER')")
    fun deactivate(
        auth: Authentication,
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Unit>> {
        productService.deactivate(SecurityUtils.getUserId(auth), id)
        return ResponseEntity.ok(ApiResponse.ok("Product deactivated"))
    }

    @Operation(
        summary = "Thêm đơn vị tính",
        description = "1 sản phẩm có thể có nhiều đơn vị (Bao, Kg...).",
    )
    @PostMapping("/{id}/units")
    @PreAuthorize("hasRole('OWNER')")
    fun addUnit(
        @PathVariable id: UUID,
        @RequestParam unit: String,
        @RequestParam price: BigDecimal,
        @RequestParam(required = false) conversionRate: BigDecimal?,
    ): ResponseEntity<ApiResponse<Unit>> {
        productService.addUnit(id, unit, price, conversionRate)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Unit added"))
    }

    @Operation(
        summary = "Xóa đơn vị tính",
        description = "Không thể xóa unit cuối cùng.",
    )
    @DeleteMapping("/{id}/units/{unitId}")
    @PreAuthorize("hasRole('OWNER')")
    fun removeUnit(
        @PathVariable id: UUID,
        @PathVariable unitId: UUID,
    ): ResponseEntity<ApiResponse<Unit>> {
        productService.removeUnit(id, unitId)
        return ResponseEntity.ok(ApiResponse.ok("Unit removed"))
    }
}
