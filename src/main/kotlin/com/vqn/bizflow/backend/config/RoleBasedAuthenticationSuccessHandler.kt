package com.vqn.bizflow.backend.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.stereotype.Component

/**
 * RoleBasedAuthenticationSuccessHandler — Điều hướng sau login.
 *
 * Cơ chế hoạt động:
 * 1. Nếu có SavedRequest từ OIDC flow (/oauth2/authorize*) → ưu tiên redirect về
 *    URL đã lưu (Spring Boot tiếp tục OIDC flow).
 * 2. Nếu không có SavedRequest → redirect về /redirect-dashboard để Spring Boot
 *    quyết định dashboard dựa trên role (single source of truth).
 */
@Component
class RoleBasedAuthenticationSuccessHandler : SavedRequestAwareAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val requestCache = HttpSessionRequestCache()
        val savedRequest = requestCache.getRequest(request, response)
        val isOAuthFlow = savedRequest?.redirectUrl?.contains("/oauth2/authorize") == true

        if (!isOAuthFlow) {
            // Không phải OAuth flow → xoá stale request + redirect về central endpoint
            requestCache.removeRequest(request, response)
            defaultTargetUrl = "/dispatch"
        }
        // OAuth flow: giữ saved request → OIDC flow tiếp tục bình thường

        super.onAuthenticationSuccess(request, response, authentication)
    }
}
