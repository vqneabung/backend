package com.vqn.bizflow.backend.platform.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vqn.bizflow.backend.exception.DuplicateException
import com.vqn.bizflow.backend.exception.ResourceNotFoundException
import com.vqn.bizflow.backend.platform.dto.AnnouncementResponse
import com.vqn.bizflow.backend.platform.dto.CreateAnnouncementRequest
import com.vqn.bizflow.backend.platform.dto.CreateReportTemplateRequest
import com.vqn.bizflow.backend.platform.dto.CreateSubscriptionPlanRequest
import com.vqn.bizflow.backend.platform.dto.ReportTemplateResponse
import com.vqn.bizflow.backend.platform.dto.SubscriptionPlanResponse
import com.vqn.bizflow.backend.platform.dto.UpdateAnnouncementRequest
import com.vqn.bizflow.backend.platform.dto.UpdateReportTemplateRequest
import com.vqn.bizflow.backend.platform.dto.UpdateSubscriptionPlanRequest
import com.vqn.bizflow.backend.platform.entity.AnnouncementEntity
import com.vqn.bizflow.backend.platform.entity.ReportTemplateEntity
import com.vqn.bizflow.backend.platform.entity.SubscriptionPlanEntity
import com.vqn.bizflow.backend.platform.mapper.PlatformMapper
import com.vqn.bizflow.backend.platform.repository.AnnouncementRepository
import com.vqn.bizflow.backend.platform.repository.ReportTemplateRepository
import com.vqn.bizflow.backend.platform.repository.SubscriptionPlanRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional(readOnly = true)
class PlatformService(
    private val subscriptionPlanRepository: SubscriptionPlanRepository,
    private val reportTemplateRepository: ReportTemplateRepository,
    private val announcementRepository: AnnouncementRepository,
    private val platformMapper: PlatformMapper,
    private val objectMapper: ObjectMapper,
) {
    fun listSubscriptionPlans(): List<SubscriptionPlanResponse> =
        subscriptionPlanRepository
            .findByIsActiveTrueOrderBySortOrderAsc()
            .map { platformMapper.toSubscriptionPlanResponse(it, objectMapper) }

    fun listReportTemplates(): List<ReportTemplateResponse> =
        reportTemplateRepository
            .findByIsActiveTrue()
            .map { platformMapper.toReportTemplateResponse(it, objectMapper) }

    fun listAnnouncements(audience: String): List<AnnouncementResponse> {
        val audiences = listOf("all", audience)
        return announcementRepository
            .findByIsPublishedTrueAndAudienceInOrderByPublishedAtDesc(audiences)
            .map(platformMapper::toAnnouncementResponse)
    }

    @Transactional
    fun createSubscriptionPlan(request: CreateSubscriptionPlanRequest): SubscriptionPlanResponse {
        if (subscriptionPlanRepository.existsBySlug(request.slug)) {
            throw DuplicateException("Subscription plan with slug '${request.slug}' already exists")
        }
        val entity = SubscriptionPlanEntity(
            name = request.name.trim(),
            slug = request.slug.trim(),
            monthlyPrice = request.monthlyPrice,
            annualPrice = request.annualPrice,
            currency = request.currency.trim(),
            features = objectMapper.writeValueAsString(request.features),
            isActive = request.isActive,
            sortOrder = request.sortOrder,
        )
        val saved = subscriptionPlanRepository.save(entity)
        return platformMapper.toSubscriptionPlanResponse(saved, objectMapper)
    }

    @Transactional
    fun updateSubscriptionPlan(id: UUID, request: UpdateSubscriptionPlanRequest): SubscriptionPlanResponse {
        val entity = subscriptionPlanRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Subscription plan not found: $id") }
        request.name?.let { entity.name = it.trim() }
        request.slug?.let {
            if (subscriptionPlanRepository.existsBySlugAndIdNot(it, id)) {
                throw DuplicateException("Subscription plan with slug '$it' already exists")
            }
            entity.slug = it.trim()
        }
        request.monthlyPrice?.let { entity.monthlyPrice = it }
        request.annualPrice?.let { entity.annualPrice = it }
        request.currency?.let { entity.currency = it.trim() }
        request.features?.let { entity.features = objectMapper.writeValueAsString(it) }
        request.isActive?.let { entity.isActive = it }
        request.sortOrder?.let { entity.sortOrder = it }
        val saved = subscriptionPlanRepository.save(entity)
        return platformMapper.toSubscriptionPlanResponse(saved, objectMapper)
    }

    @Transactional
    fun deleteSubscriptionPlan(id: UUID) {
        if (!subscriptionPlanRepository.existsById(id)) {
            throw ResourceNotFoundException("Subscription plan not found: $id")
        }
        subscriptionPlanRepository.deleteById(id)
    }

    @Transactional
    fun createReportTemplate(request: CreateReportTemplateRequest): ReportTemplateResponse {
        if (reportTemplateRepository.existsByCode(request.code)) {
            throw DuplicateException("Report template with code '${request.code}' already exists")
        }
        val entity = ReportTemplateEntity(
            name = request.name.trim(),
            code = request.code.trim(),
            description = request.description?.trim(),
            circularRef = request.circularRef.trim(),
            version = request.version.trim(),
            fields = objectMapper.writeValueAsString(request.fields),
            isActive = request.isActive,
        )
        val saved = reportTemplateRepository.save(entity)
        return platformMapper.toReportTemplateResponse(saved, objectMapper)
    }

    @Transactional
    fun updateReportTemplate(id: UUID, request: UpdateReportTemplateRequest): ReportTemplateResponse {
        val entity = reportTemplateRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Report template not found: $id") }
        request.name?.let { entity.name = it.trim() }
        request.code?.let {
            if (reportTemplateRepository.existsByCodeAndIdNot(it, id)) {
                throw DuplicateException("Report template with code '$it' already exists")
            }
            entity.code = it.trim()
        }
        request.description?.let { entity.description = it.trim() }
        request.circularRef?.let { entity.circularRef = it.trim() }
        request.version?.let { entity.version = it.trim() }
        request.fields?.let { entity.fields = objectMapper.writeValueAsString(it) }
        request.isActive?.let { entity.isActive = it }
        val saved = reportTemplateRepository.save(entity)
        return platformMapper.toReportTemplateResponse(saved, objectMapper)
    }

    @Transactional
    fun deleteReportTemplate(id: UUID) {
        if (!reportTemplateRepository.existsById(id)) {
            throw ResourceNotFoundException("Report template not found: $id")
        }
        reportTemplateRepository.deleteById(id)
    }

    @Transactional
    fun createAnnouncement(request: CreateAnnouncementRequest): AnnouncementResponse {
        val entity = AnnouncementEntity(
            title = request.title.trim(),
            message = request.message.trim(),
            audience = request.audience.trim(),
            priority = request.priority.trim(),
            isPublished = request.isPublished,
            publishedAt = if (request.isPublished) Instant.now() else null,
            expiresAt = request.expiresAt,
            createdBy = request.createdBy?.trim(),
        )
        val saved = announcementRepository.save(entity)
        return platformMapper.toAnnouncementResponse(saved)
    }

    @Transactional
    fun updateAnnouncement(id: UUID, request: UpdateAnnouncementRequest): AnnouncementResponse {
        val entity = announcementRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Announcement not found: $id") }
        request.title?.let { entity.title = it.trim() }
        request.message?.let { entity.message = it.trim() }
        request.audience?.let { entity.audience = it.trim() }
        request.priority?.let { entity.priority = it.trim() }
        request.isPublished?.let {
            entity.isPublished = it
            if (it && entity.publishedAt == null) {
                entity.publishedAt = Instant.now()
            }
        }
        request.expiresAt?.let { entity.expiresAt = it }
        request.createdBy?.let { entity.createdBy = it.trim() }
        val saved = announcementRepository.save(entity)
        return platformMapper.toAnnouncementResponse(saved)
    }

    @Transactional
    fun deleteAnnouncement(id: UUID) {
        if (!announcementRepository.existsById(id)) {
            throw ResourceNotFoundException("Announcement not found: $id")
        }
        announcementRepository.deleteById(id)
    }
}
