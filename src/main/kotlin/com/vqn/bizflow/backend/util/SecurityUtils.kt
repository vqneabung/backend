package com.vqn.bizflow.backend.util

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import java.util.UUID

/**
 * Tiện ích xử lý Authentication.
 *
 * Principal chứa user ID dạng UUID string.
 */
object SecurityUtils {

    /** Lấy user ID (UUID) từ JWT principal (subject/sub claim). */
    fun getUserId(auth: Authentication): UUID =
        UUID.fromString(auth.name)

    /**
     * Xác định URL redirect sau login dựa trên role.
     * Dùng chung cho RoleBasedAuthenticationSuccessHandler và LoginController
     * để tránh duplicate logic.
     */
    fun determineRedirectUrl(auth: Authentication, frontendUrl: String, adminUrl: String): String {
        val roles = auth.authorities.map(GrantedAuthority::getAuthority)
        return when {
            "ROLE_ADMIN" in roles -> "$adminUrl/admin"
            "ROLE_OWNER" in roles -> "$frontendUrl/vi/dashboard"
            else -> frontendUrl
        }
    }
}
