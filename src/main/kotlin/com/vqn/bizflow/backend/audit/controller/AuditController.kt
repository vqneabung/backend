package com.vqn.bizflow.backend.audit.controller

import com.vqn.bizflow.backend.audit.dto.AuditLogResponse
import com.vqn.bizflow.backend.audit.service.AuditService
import com.vqn.bizflow.backend.dto.ApiResponse
import com.vqn.bizflow.backend.dto.PaginationResponse
import com.vqn.bizflow.backend.util.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Audit", description = "Audit log - theo dõi thao tác CUD")
@RestController
@RequestMapping("/api/audit")
class AuditController(private val auditService: AuditService) {

    @Operation(
        summary = "Danh sách audit log",
        description = "Phân trang, lọc theo entityType optional.",
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun list(
        auth: Authentication,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) entityType: String?,
    ): ResponseEntity<ApiResponse<PaginationResponse<AuditLogResponse>>> {
        val result = auditService.list(
            ownerId = SecurityUtils.getUserId(auth),
            page = page,
            size = size,
            entityType = entityType,
        )
        return ResponseEntity.ok(ApiResponse.success(result))
    }
}
