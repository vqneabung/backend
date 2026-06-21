package com.vqn.bizflow.backend.platform.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

@Schema(description = "Yêu cầu tạo thông báo mới")
data class CreateAnnouncementRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    @field:Schema(description = "Tiêu đề thông báo", example = "Bảo trì hệ thống")
    val title: String,

    @field:NotBlank(message = "Message is required")
    @field:Schema(description = "Nội dung thông báo", example = "Hệ thống sẽ bảo trì từ 22:00 đến 02:00")
    val message: String,

    @field:Size(max = 30, message = "Audience must not exceed 30 characters")
    @field:Schema(description = "Đối tượng nhận thông báo", example = "all")
    val audience: String = "all",

    @field:Size(max = 20, message = "Priority must not exceed 20 characters")
    @field:Schema(description = "Mức độ ưu tiên", example = "normal")
    val priority: String = "normal",

    @field:Schema(description = "Công khai ngay", example = "false")
    val isPublished: Boolean = false,

    @field:Schema(description = "Thời điểm hết hạn")
    val expiresAt: Instant? = null,

    @field:Size(max = 255, message = "Created by must not exceed 255 characters")
    @field:Schema(description = "Người tạo", example = "admin")
    val createdBy: String? = null,
)
