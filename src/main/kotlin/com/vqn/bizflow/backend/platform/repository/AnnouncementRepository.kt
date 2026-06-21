package com.vqn.bizflow.backend.platform.repository

import com.vqn.bizflow.backend.platform.entity.AnnouncementEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AnnouncementRepository : JpaRepository<AnnouncementEntity, UUID> {

    fun findByIsPublishedTrueAndAudienceInOrderByPublishedAtDesc(
        audiences: List<String>,
    ): List<AnnouncementEntity>
}
