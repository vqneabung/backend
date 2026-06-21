package com.vqn.bizflow.backend.platform.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal

@Schema(description = "Yêu cầu cập nhật gói đăng ký")
data class UpdateSubscriptionPlanRequest(
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    @field:Schema(description = "Tên gói", example = "Starter")
    val name: String? = null,

    @field:Size(max = 100, message = "Slug must not exceed 100 characters")
    @field:Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, digits, and hyphens")
    @field:Schema(description = "Slug duy nhất", example = "starter")
    val slug: String? = null,

    @field:DecimalMin(value = "0", message = "Monthly price cannot be negative")
    @field:Schema(description = "Giá tháng (VND)", example = "0")
    val monthlyPrice: BigDecimal? = null,

    @field:DecimalMin(value = "0", message = "Annual price cannot be negative")
    @field:Schema(description = "Giá năm (VND)", example = "0")
    val annualPrice: BigDecimal? = null,

    @field:Size(max = 3, message = "Currency code must be 3 characters")
    @field:Schema(description = "Mã tiền tệ ISO 4217", example = "VND")
    val currency: String? = null,

    @field:Schema(description = "Danh sách tính năng (mảng JSON)")
    val features: List<String>? = null,

    @field:Schema(description = "Kích hoạt", example = "true")
    val isActive: Boolean? = null,

    @field:Schema(description = "Thứ tự sắp xếp", example = "1")
    val sortOrder: Int? = null,
)
