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

private fun Authentication.isAdmin(): Boolean =
    authorities.any { it.authority == "ROLE_ADMIN" }

/**
 * ProductController — REST API quản lý sản phẩm.
 *
 * Phân quyền:
 * - Tất cả endpoints yêu cầu authentication (JWT)
 * - CREATE/UPDATE/DELETE: yêu cầu role USER hoặc ADMIN
 * - READ: tất cả roles
 */
@Tag(name = "Products", description = "Quản lý sản phẩm")
@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService,
) {
    @Operation(
        summary = "Tạo sản phẩm mới",
        description = "USER và ADMIN được tạo sản phẩm. Kiểm tra trùng tên, barcode.",
    )
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "201", description = "Tạo thành công"),
        SwaggerApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        SwaggerApiResponse(responseCode = "409", description = "Tên sản phẩm đã tồn tại"),
    ])
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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
        description = "Phân trang, tìm kiếm, lọc theo categoryId. Tất cả roles đều xem được.",
    )
    @GetMapping
    fun list(
        auth: Authentication,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) categoryId: UUID?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sortBy: String?,
        @RequestParam(defaultValue = "desc") sortDir: String?,
    ): ResponseEntity<PaginationResponse<ProductResponse>> {
        val isAdmin = auth.isAdmin()
        val result = productService.list(
            SecurityUtils.getUserId(auth), search, categoryId, page, size, sortBy, sortDir, isAdmin
        )
        return ResponseEntity.ok(result)
    }

    @Operation(summary = "Chi tiết sản phẩm")
    @GetMapping("/{id}")
    fun getById(
        auth: Authentication,
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<ProductResponse>> {
        val result = productService.getById(SecurityUtils.getUserId(auth), id)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @Operation(
        summary = "Cập nhật sản phẩm",
        description = "USER và ADMIN được sửa. Có optimistic locking (version).",
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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
        description = "USER và ADMIN. Không xóa vật lý.",
    )
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun deactivate(
        auth: Authentication,
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Unit>> {
        productService.deactivate(SecurityUtils.getUserId(auth), id)
        return ResponseEntity.ok(ApiResponse.ok("Product deactivated"))
    }

    @Operation(
        summary = "Thêm đơn vị tính phụ",
        description = "1 sản phẩm có thể có nhiều đơn vị (Bao, Kg...). unitId là FK → units(id).",
    )
    @PostMapping("/{id}/units")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun addUnit(
        auth: Authentication,
        @PathVariable id: UUID,
        @RequestParam unitId: UUID,
        @RequestParam price: BigDecimal,
        @RequestParam(required = false) conversionRate: BigDecimal?,
    ): ResponseEntity<ApiResponse<Unit>> {
        productService.addUnit(SecurityUtils.getUserId(auth), id, unitId, price, conversionRate)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Unit added"))
    }

    @Operation(
        summary = "Xóa đơn vị tính phụ",
        description = "Không thể xóa unit cuối cùng.",
    )
    @DeleteMapping("/{id}/units/{unitId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun removeUnit(
        auth: Authentication,
        @PathVariable id: UUID,
        @PathVariable unitId: UUID,
    ): ResponseEntity<ApiResponse<Unit>> {
        productService.removeUnit(SecurityUtils.getUserId(auth), id, unitId)
        return ResponseEntity.ok(ApiResponse.ok("Unit removed"))
    }
}
