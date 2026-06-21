package com.vqn.bizflow.backend.platform.mapper

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.vqn.bizflow.backend.platform.dto.AnnouncementResponse
import com.vqn.bizflow.backend.platform.dto.ReportTemplateField
import com.vqn.bizflow.backend.platform.dto.ReportTemplateResponse
import com.vqn.bizflow.backend.platform.dto.SubscriptionPlanResponse
import com.vqn.bizflow.backend.platform.entity.AnnouncementEntity
import com.vqn.bizflow.backend.platform.entity.ReportTemplateEntity
import com.vqn.bizflow.backend.platform.entity.SubscriptionPlanEntity
import org.mapstruct.Context
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.slf4j.LoggerFactory

@Mapper(componentModel = "spring")
abstract class PlatformMapper {

    @Mapping(target = "features", expression = "java(parseStringList(entity.getFeatures(), objectMapper))")
    abstract fun toSubscriptionPlanResponse(
        entity: SubscriptionPlanEntity,
        @Context objectMapper: ObjectMapper,
    ): SubscriptionPlanResponse

    @Mapping(target = "fields", expression = "java(parseFields(entity.getFields(), objectMapper))")
    abstract fun toReportTemplateResponse(
        entity: ReportTemplateEntity,
        @Context objectMapper: ObjectMapper,
    ): ReportTemplateResponse

    abstract fun toAnnouncementResponse(entity: AnnouncementEntity): AnnouncementResponse

    protected fun parseStringList(json: String?, objectMapper: ObjectMapper): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            objectMapper.readValue(json, object : TypeReference<List<String>>() {})
        }.getOrElse {
            log.warn("Failed to parse subscription plan features JSON, returning empty list", it)
            emptyList()
        }
    }

    protected fun parseFields(json: String?, objectMapper: ObjectMapper): List<ReportTemplateField> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            objectMapper.readValue(json, object : TypeReference<List<ReportTemplateField>>() {})
        }.getOrElse {
            log.warn("Failed to parse report template fields JSON, returning empty list", it)
            emptyList()
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(PlatformMapper::class.java)
    }
}
