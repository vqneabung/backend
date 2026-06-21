package com.vqn.bizflow.backend.order.controller

import com.vqn.bizflow.backend.dto.ApiResponse
import com.vqn.bizflow.backend.dto.PaginationResponse
import com.vqn.bizflow.backend.order.dto.CreateOrderRequest
import com.vqn.bizflow.backend.order.dto.OrderResponse
import com.vqn.bizflow.backend.order.dto.OrderSummaryResponse
import com.vqn.bizflow.backend.order.dto.UpdateOrderStatusRequest
import com.vqn.bizflow.backend.order.service.OrderService
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
import java.time.Instant
import java.util.UUID

/**
 * OrderController — REST API quản lý đơn hàng.
 *
 * Endpoints:
 * - POST /api/orders — tạo đơn (DRAFT hoặc CONFIRMED → trừ kho)
 * - GET  /api/orders — list (phân trang, filter status/date)
 * - GET  /api/orders/{id} — chi tiết (kèm items)
 * - PATCH /api/orders/{id}/cancel — hủy đơn (hoàn kho nếu CONFIRMED)
 */
@Tag(name = "Orders", description = "Quản lý đơn hàng bán tại quầy")
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @Operation(
        summary = "Tạo đơn hàng",
        description = """
            Tạo đơn hàng mới. 
            status = DRAFT: lưu nháp, không ảnh hưởng kho.
            status = CONFIRMED: tự động trừ kho từng sản phẩm (FR-14).
        """,
    )
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "201", description = "Tạo thành công"),
        SwaggerApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ / không đủ tồn kho"),
        SwaggerApiResponse(responseCode = "404", description = "Sản phẩm không tồn tại"),
    ])
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'ADMIN')")
    fun create(
        auth: Authentication,
        @Valid @RequestBody request: CreateOrderRequest,
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        val response = orderService.create(SecurityUtils.getUserId(auth), request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(response, "Order created"))
    }

    private fun Authentication.isAdmin(): Boolean =
        authorities.any { it.authority == "ROLE_ADMIN" }

    @Operation(
        summary = "Danh sách đơn hàng",
        description = "Phân trang, filter theo status và khoảng thời gian.",
    )
    @GetMapping
    fun list(
        auth: Authentication,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) fromDate: Instant?,
        @RequestParam(required = false) toDate: Instant?,
    ): ResponseEntity<PaginationResponse<OrderSummaryResponse>> {
        val result = orderService.list(
            SecurityUtils.getUserId(auth), page, size, status, fromDate, toDate, isAdmin = auth.isAdmin(),
        )
        return ResponseEntity.ok(result)
    }

    @Operation(
        summary = "Chi tiết đơn hàng",
        description = "Kèm danh sách items.",
    )
    @GetMapping("/{id}")
    fun getById(
        auth: Authentication,
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        val result = orderService.getById(SecurityUtils.getUserId(auth), id, isAdmin = auth.isAdmin())
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @Operation(
        summary = "Hủy đơn hàng",
        description = """
            Hủy đơn hàng. 
            Nếu đơn đang là CONFIRMED → tự động hoàn lại stock (FR-14).
            KHÔNG support hoàn tác sau khi hủy.
        """,
    )
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Hủy thành công"),
        SwaggerApiResponse(responseCode = "400", description = "Đơn đã hủy / không thể hủy"),
        SwaggerApiResponse(responseCode = "404", description = "Không tìm thấy đơn"),
    ])
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'ADMIN')")
    fun cancel(
        auth: Authentication,
        @PathVariable id: UUID,
        @RequestBody request: UpdateOrderStatusRequest?,
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        val result = orderService.cancel(
            SecurityUtils.getUserId(auth), id, request?.notes, isAdmin = auth.isAdmin(),
        )
        return ResponseEntity.ok(ApiResponse.success(result, "Order cancelled"))
    }
}
