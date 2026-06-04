package com.vqn.bizflow.backend.util

import org.springframework.security.core.Authentication
import java.util.UUID

/**
 * Tiện ích xử lý Authentication.
 */
object SecurityUtils {

    /** Lấy user ID (UUID) từ JWT principal (subject/sub claim). */
    fun getUserId(auth: Authentication): UUID =
        UUID.fromString(auth.name)
}
