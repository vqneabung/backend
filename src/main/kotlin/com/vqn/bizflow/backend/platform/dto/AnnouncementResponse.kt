package com.vqn.bizflow.backend.platform.dto

import java.time.Instant
import java.util.UUID

data class AnnouncementResponse(
    val id: UUID,
    val title: String,
    val message: String,
    val audience: String,
    val priority: String,
    val isPublished: Boolean,
    val publishedAt: Instant?,
    val expiresAt: Instant?,
    val createdBy: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)
