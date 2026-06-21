package com.vqn.bizflow.backend.platform.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import java.math.BigDecimal

@Schema(description = "Yêu cầu tạo gói đăng ký mới")
data class CreateSubscriptionPlanRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    @field:Schema(description = "Tên gói", example = "Starter")
    val name: String,

    @field:NotBlank(message = "Slug is required")
    @field:Size(max = 100, message = "Slug must not exceed 100 characters")
    @field:Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, digits, and hyphens")
    @field:Schema(description = "Slug duy nhất", example = "starter")
    val slug: String,

    @field:NotNull(message = "Monthly price is required")
    @field:DecimalMin(value = "0", message = "Monthly price cannot be negative")
    @field:Schema(description = "Giá tháng (VND)", example = "0")
    val monthlyPrice: BigDecimal,

    @field:NotNull(message = "Annual price is required")
    @field:DecimalMin(value = "0", message = "Annual price cannot be negative")
    @field:Schema(description = "Giá năm (VND)", example = "0")
    val annualPrice: BigDecimal,

    @field:Size(max = 3, message = "Currency code must be 3 characters")
    @field:Schema(description = "Mã tiền tệ ISO 4217", example = "VND")
    val currency: String = "VND",

    @field:Schema(description = "Danh sách tính năng (mảng JSON)")
    val features: List<String> = emptyList(),

    @field:Schema(description = "Kích hoạt", example = "true")
    val isActive: Boolean = true,

    @field:Schema(description = "Thứ tự sắp xếp", example = "1")
    val sortOrder: Int = 0,
)
