package com.vqn.bizflow.backend.controller

import com.vqn.bizflow.backend.dto.ApiResponse
import com.vqn.bizflow.backend.dto.PaginationResponse
import com.vqn.bizflow.backend.inventory.dto.CreateStockImportRequest
import com.vqn.bizflow.backend.inventory.dto.StockImportResponse
import com.vqn.bizflow.backend.inventory.dto.StockImportSummaryResponse
import com.vqn.bizflow.backend.inventory.service.StockImportService
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
 * StockImportController — REST API quản lý nhập kho.
 *
 * Endpoints:
 * - POST /api/stock-imports — tạo phiếu nhập + tăng stock
 * - GET  /api/stock-imports — list (phân trang, mới nhất trước)
 * - GET  /api/stock-imports/{id} — chi tiết (kèm items)
 *
 * phiếu nhập KHÔNG support sửa/xóa sau khi tạo (stock đã cập nhật).
 */
@Tag(name = "Stock Imports", description = "Quản lý nhập kho")
@RestController
@RequestMapping("/api/stock-imports")
class StockImportController(
    private val stockImportService: StockImportService,
) {
    @Operation(
        summary = "Tạo phiếu nhập kho",
        description = "Tự động tăng tồn kho cho từng sản phẩm. USER và ADMIN.",
    )
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "201", description = "Tạo thành công"),
        SwaggerApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        SwaggerApiResponse(responseCode = "404", description = "Sản phẩm không tồn tại"),
    ])
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun create(
        auth: Authentication,
        @Valid @RequestBody request: CreateStockImportRequest,
    ): ResponseEntity<ApiResponse<StockImportResponse>> {
        val response = stockImportService.create(SecurityUtils.getUserId(auth), request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(response, "Stock import created"))
    }

    @Operation(
        summary = "Danh sách phiếu nhập",
        description = "Phân trang, mới nhất trước.",
    )
    @GetMapping
    fun list(
        auth: Authentication,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<PaginationResponse<StockImportSummaryResponse>> {
        val result = stockImportService.list(SecurityUtils.getUserId(auth), page, size)
        return ResponseEntity.ok(result)
    }

    @Operation(
        summary = "Chi tiết phiếu nhập",
        description = "Kèm danh sách items với tên sản phẩm.",
    )
    @GetMapping("/{id}")
    fun getById(
        auth: Authentication,
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<StockImportResponse>> {
        val result = stockImportService.getById(SecurityUtils.getUserId(auth), id)
        return ResponseEntity.ok(ApiResponse.success(result))
    }
}
