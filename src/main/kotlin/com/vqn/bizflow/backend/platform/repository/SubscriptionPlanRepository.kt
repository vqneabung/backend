package com.vqn.bizflow.backend.platform.repository

import com.vqn.bizflow.backend.platform.entity.SubscriptionPlanEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SubscriptionPlanRepository : JpaRepository<SubscriptionPlanEntity, UUID> {

    fun findByIsActiveTrueOrderBySortOrderAsc(): List<SubscriptionPlanEntity>

    fun existsBySlug(slug: String): Boolean

    fun existsBySlugAndIdNot(slug: String, id: UUID): Boolean
}
