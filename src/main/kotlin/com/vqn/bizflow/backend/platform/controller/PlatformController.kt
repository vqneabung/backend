package com.vqn.bizflow.backend.platform.controller

import com.vqn.bizflow.backend.dto.ApiResponse
import com.vqn.bizflow.backend.platform.dto.AnnouncementResponse
import com.vqn.bizflow.backend.platform.dto.CreateAnnouncementRequest
import com.vqn.bizflow.backend.platform.dto.CreateReportTemplateRequest
import com.vqn.bizflow.backend.platform.dto.CreateSubscriptionPlanRequest
import com.vqn.bizflow.backend.platform.dto.ReportTemplateResponse
import com.vqn.bizflow.backend.platform.dto.SubscriptionPlanResponse
import com.vqn.bizflow.backend.platform.dto.UpdateAnnouncementRequest
import com.vqn.bizflow.backend.platform.dto.UpdateReportTemplateRequest
import com.vqn.bizflow.backend.platform.dto.UpdateSubscriptionPlanRequest
import com.vqn.bizflow.backend.platform.service.PlatformService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(
    name = "Platform",
    description = "Platform-level data (subscription plans, report templates, announcements)",
)
@RestController
@RequestMapping("/api/platform")
class PlatformController(
    private val platformService: PlatformService,
) {
    @Operation(summary = "Danh sách gói subscription đang active (sắp xếp theo sortOrder)")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Thành công"),
        SwaggerApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
    ])
    @GetMapping("/subscription-plans")
    @PreAuthorize("isAuthenticated()")
    fun listSubscriptionPlans(): ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> =
        ResponseEntity.ok(ApiResponse.success(platformService.listSubscriptionPlans()))

    @Operation(summary = "Tạo gói subscription mới")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "201", description = "Đã tạo"),
        SwaggerApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        SwaggerApiResponse(responseCode = "403", description = "Không có quyền ADMIN"),
        SwaggerApiResponse(responseCode = "409", description = "Slug đã tồn tại"),
    ])
    @PostMapping("/subscription-plans")
    @PreAuthorize("hasRole('ADMIN')")
    fun createSubscriptionPlan(
        @Valid @RequestBody request: CreateSubscriptionPlanRequest,
    ): ResponseEntity<ApiResponse<SubscriptionPlanResponse>> {
        val response = platformService.createSubscriptionPlan(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response))
    }

    @Operation(summary = "Cập nhật gói subscription")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Thành công"),
        SwaggerApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        SwaggerApiResponse(responseCode = "403", description = "Không có quyền ADMIN"),
        SwaggerApiResponse(responseCode = "404", description = "Không tìm thấy"),
        SwaggerApiResponse(responseCode = "409", description = "Slug đã tồn tại"),
    ])
    @PutMapping("/subscription-plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateSubscriptionPlan(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateSubscriptionPlanRequest,
    ): ResponseEntity<ApiResponse<SubscriptionPlanResponse>> =
        ResponseEntity.ok(ApiResponse.success(platformService.updateSubscriptionPlan(id, request)))

    @Operation(summary = "Xóa gói subscription")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Đã xóa"),
        SwaggerApiResponse(responseCode = "403", description = "Không có quyền ADMIN"),
        SwaggerApiResponse(responseCode = "404", description = "Không tìm thấy"),
    ])
    @DeleteMapping("/subscription-plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteSubscriptionPlan(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        platformService.deleteSubscriptionPlan(id)
        return ResponseEntity.ok(ApiResponse.ok())
    }

    @Operation(summary = "Danh sách report template đang active")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Thành công"),
        SwaggerApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
    ])
    @GetMapping("/report-templates")
    @PreAuthorize("isAuthenticated()")
    fun listReportTemplates(): ResponseEntity<ApiResponse<List<ReportTemplateResponse>>> =
        ResponseEntity.ok(ApiResponse.success(platformService.listReportTemplates()))

    @Operation(summary = "Tạo report template mới")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "201", description = "Đã tạo"),
        SwaggerApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        SwaggerApiResponse(responseCode = "403", description = "Không có quyền ADMIN"),
        SwaggerApiResponse(responseCode = "409", description = "Code đã tồn tại"),
    ])
    @PostMapping("/report-templates")
    @PreAuthorize("hasRole('ADMIN')")
    fun createReportTemplate(
        @Valid @RequestBody request: CreateReportTemplateRequest,
    ): ResponseEntity<ApiResponse<ReportTemplateResponse>> {
        val response = platformService.createReportTemplate(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response))
    }

    @Operation(summary = "Cập nhật report template")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Thành công"),
        SwaggerApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        SwaggerApiResponse(responseCode = "403", description = "Không có quyền ADMIN"),
        SwaggerApiResponse(responseCode = "404", description = "Không tìm thấy"),
        SwaggerApiResponse(responseCode = "409", description = "Code đã tồn tại"),
    ])
    @PutMapping("/report-templates/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateReportTemplate(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateReportTemplateRequest,
    ): ResponseEntity<ApiResponse<ReportTemplateResponse>> =
        ResponseEntity.ok(ApiResponse.success(platformService.updateReportTemplate(id, request)))

    @Operation(summary = "Xóa report template")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Đã xóa"),
        SwaggerApiResponse(responseCode = "403", description = "Không có quyền ADMIN"),
        SwaggerApiResponse(responseCode = "404", description = "Không tìm thấy"),
    ])
    @DeleteMapping("/report-templates/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteReportTemplate(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        platformService.deleteReportTemplate(id)
        return ResponseEntity.ok(ApiResponse.ok())
    }

    @Operation(summary = "Danh sách thông báo đang published (lọc theo audience)")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Thành công"),
        SwaggerApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
    ])
    @GetMapping("/announcements")
    @PreAuthorize("isAuthenticated()")
    fun listAnnouncements(
        @RequestParam(defaultValue = "all") audience: String,
    ): ResponseEntity<ApiResponse<List<AnnouncementResponse>>> =
        ResponseEntity.ok(ApiResponse.success(platformService.listAnnouncements(audience)))

    @Operation(summary = "Tạo thông báo mới")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "201", description = "Đã tạo"),
        SwaggerApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        SwaggerApiResponse(responseCode = "403", description = "Không có quyền ADMIN"),
    ])
    @PostMapping("/announcements")
    @PreAuthorize("hasRole('ADMIN')")
    fun createAnnouncement(
        @Valid @RequestBody request: CreateAnnouncementRequest,
    ): ResponseEntity<ApiResponse<AnnouncementResponse>> {
        val response = platformService.createAnnouncement(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response))
    }

    @Operation(summary = "Cập nhật thông báo")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Thành công"),
        SwaggerApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        SwaggerApiResponse(responseCode = "403", description = "Không có quyền ADMIN"),
        SwaggerApiResponse(responseCode = "404", description = "Không tìm thấy"),
    ])
    @PutMapping("/announcements/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateAnnouncement(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateAnnouncementRequest,
    ): ResponseEntity<ApiResponse<AnnouncementResponse>> =
        ResponseEntity.ok(ApiResponse.success(platformService.updateAnnouncement(id, request)))

    @Operation(summary = "Xóa thông báo")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Đã xóa"),
        SwaggerApiResponse(responseCode = "403", description = "Không có quyền ADMIN"),
        SwaggerApiResponse(responseCode = "404", description = "Không tìm thấy"),
    ])
    @DeleteMapping("/announcements/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteAnnouncement(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        platformService.deleteAnnouncement(id)
        return ResponseEntity.ok(ApiResponse.ok())
    }
}
