package com.vqn.bizflow.backend.config

import com.vqn.bizflow.backend.util.SecurityUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.stereotype.Component

/**
 * RoleBasedAuthenticationSuccessHandler — Điều hướng sau login theo role.
 *
 * Cơ chế hoạt động:
 * 1. Nếu có SavedRequest (từ OIDC flow) → ưu tiên redirect về URL đã lưu
 *    (Spring Boot /oauth2/authorize tiếp tục OIDC flow → redirect frontend callback)
 * 2. Nếu không có SavedRequest (user vô thẳng /login) → redirect theo role:
 *    - ROLE_ADMIN → PHP Laravel admin (localhost:8000/admin)
 *    - ROLE_OWNER → Next.js dashboard (localhost:3000/vi/dashboard)
 *
 * Flow direct login → role-based redirect:
 * 1. Login thành công → JSESSIONID set
 * 2. Redirect đến frontend URL theo role
 * 3. Frontend detect chưa có token → tự initiate OIDC flow
 * 4. Spring Boot thấy JSESSIONID → auto-authorize (không cần login lại)
 * 5. Frontend nhận code → exchange token → dashboard
 */
@Component
class RoleBasedAuthenticationSuccessHandler(
    @Value("\${app.frontend-url}") private val frontendUrl: String,
    @Value("\${app.admin-url}") private val adminUrl: String,
) : SavedRequestAwareAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        // Set URL theo role làm fallback
        // SavedRequestAwareAuthenticationSuccessHandler:
        //   - Có SavedRequest (OIDC) → dùng saved URL (bỏ qua defaultTargetUrl)
        //   - Không có SavedRequest (direct login) → dùng defaultTargetUrl
        defaultTargetUrl = determineTargetUrl(authentication)
        super.onAuthenticationSuccess(request, response, authentication)
    }

    private fun determineTargetUrl(authentication: Authentication): String =
        SecurityUtils.determineRedirectUrl(authentication, frontendUrl, adminUrl)
}
