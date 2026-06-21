package com.vqn.bizflow.backend.platform.repository

import com.vqn.bizflow.backend.platform.entity.ReportTemplateEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ReportTemplateRepository : JpaRepository<ReportTemplateEntity, UUID> {

    fun findByIsActiveTrue(): List<ReportTemplateEntity>

    fun existsByCode(code: String): Boolean

    fun existsByCodeAndIdNot(code: String, id: UUID): Boolean
}
