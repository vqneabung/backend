package com.vqn.bizflow.backend.platform.dto

import java.math.BigDecimal
import java.util.UUID

data class SubscriptionPlanResponse(
    val id: UUID,
    val name: String,
    val slug: String,
    val monthlyPrice: BigDecimal,
    val annualPrice: BigDecimal,
    val currency: String,
    val features: List<String>,
    val isActive: Boolean,
    val sortOrder: Int,
)
