package com.vqn.bizflow.backend.report.controller

import com.vqn.bizflow.backend.dto.ApiResponse
import com.vqn.bizflow.backend.report.dto.*
import com.vqn.bizflow.backend.report.service.ReportService
import com.vqn.bizflow.backend.util.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

/**
 * ReportController — REST API báo cáo thống kê (FR-19 → FR-22).
 *
 * Tất cả endpoints đều read-only.
 */
@Tag(name = "Reports", description = "Báo cáo thống kê (doanh thu, tồn kho, công nợ)")
@RestController
@RequestMapping("/api/reports")
class ReportController(
    private val reportService: ReportService,
) {
    @Operation(
        summary = "Tổng quan dashboard",
        description = "Stat cards: tổng SP, đơn hàng tháng này, doanh thu tháng, tổng KH, SP sắp hết hàng.",
    )
    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun overview(auth: Authentication): ResponseEntity<ApiResponse<ReportOverviewResponse>> {
        val result = reportService.getOverview(SecurityUtils.getUserId(auth))
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @Operation(
        summary = "Doanh thu theo ngày",
        description = "Daily revenue chart data. range: 7d | 30d | thisMonth. Group by date, auto zero-fill.",
    )
    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun revenue(
        auth: Authentication,
        @RequestParam(defaultValue = "30d") range: String,
    ): ResponseEntity<ApiResponse<RevenueReportResponse>> {
        val result = reportService.getRevenue(SecurityUtils.getUserId(auth), range)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @Operation(
        summary = "Sản phẩm bán chạy",
        description = "Top N sản phẩm theo số lượng đã bán (từ đơn CONFIRMED).",
    )
    @GetMapping("/best-selling")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun bestSelling(
        auth: Authentication,
        @RequestParam(defaultValue = "10") limit: Int,
    ): ResponseEntity<ApiResponse<BestSellingReportResponse>> {
        val result = reportService.getBestSelling(SecurityUtils.getUserId(auth), limit)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @Operation(
        summary = "Tồn kho",
        description = "Tổng giá trị tồn kho, SP sắp hết, phân bổ theo danh mục.",
    )
    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun inventory(auth: Authentication): ResponseEntity<ApiResponse<InventoryReportResponse>> {
        val result = reportService.getInventory(SecurityUtils.getUserId(auth))
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @Operation(
        summary = "Công nợ khách hàng",
        description = "Danh sách khách hàng còn nợ, tổng nợ, số đơn chưa thanh toán.",
    )
    @GetMapping("/debt")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun debt(auth: Authentication): ResponseEntity<ApiResponse<DebtReportResponse>> {
        val result = reportService.getDebt(SecurityUtils.getUserId(auth))
        return ResponseEntity.ok(ApiResponse.success(result))
    }
}
