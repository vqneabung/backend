package com.vqn.bizflow.backend.platform.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "Yêu cầu tạo mẫu báo cáo mới")
data class CreateReportTemplateRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    @field:Schema(description = "Tên mẫu báo cáo", example = "Báo cáo kết quả bán hàng")
    val name: String,

    @field:NotBlank(message = "Code is required")
    @field:Size(max = 100, message = "Code must not exceed 100 characters")
    @field:Pattern(regexp = "^[A-Z0-9_]+$", message = "Code must contain only uppercase letters, digits, and underscore")
    @field:Schema(description = "Mã định danh kỹ thuật", example = "BCKH_BANHANG")
    val code: String,

    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    @field:Schema(description = "Mô tả mẫu báo cáo")
    val description: String? = null,

    @field:Size(max = 100, message = "Circular ref must not exceed 100 characters")
    @field:Schema(description = "Thông tư tham chiếu", example = "Circular 88/2021/TT-BTC")
    val circularRef: String = "Circular 88/2021/TT-BTC",

    @field:Schema(description = "Phiên bản mẫu", example = "1.0")
    val version: String = "1.0",

    @field:Schema(description = "Danh sách trường báo cáo")
    val fields: List<ReportTemplateField> = emptyList(),

    @field:Schema(description = "Kích hoạt", example = "true")
    val isActive: Boolean = true,
)
