package com.vqn.bizflow.backend.util

import org.springframework.security.core.Authentication
import java.util.UUID

/**
 * Tiện ích xử lý Authentication.
 *
 * Trích xuất thông tin user từ JWT principal.
 * Principal chứa user ID dạng UUID string.
 */
object SecurityUtils {

    /** Lấy user ID (UUID) từ JWT principal (subject/sub claim). */
    fun getUserId(auth: Authentication): UUID =
        UUID.fromString(auth.name)
}
