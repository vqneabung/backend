package com.vqn.bizflow.backend.platform.dto

import java.time.Instant
import java.util.UUID

data class ReportTemplateResponse(
    val id: UUID,
    val name: String,
    val code: String,
    val description: String?,
    val circularRef: String,
    val version: String,
    val fields: List<ReportTemplateField>,
    val isActive: Boolean,
    val lastUpdatedBy: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)

data class ReportTemplateField(
    val key: String,
    val label: String,
    val type: String,
    val width: Int,
    val alignment: String,
)
