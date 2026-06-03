package com.vqn.bizflow.backend.controller

import com.vqn.bizflow.backend.dto.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

// Session management endpoints — invalidate Spring Security session (JSESSIONID).
//
// Duoc Next.js goi khi user dang xuat, de dam bao Spring Boot session cung bi
// huy, tranh truong hop user van con authenticated o Spring (qua JSESSIONID cookie)
// => OIDC auto-authorize ngay sau khi logout.
//
// Flow:
// 1. Next.js `/api/auth/logout` (route handler) goi POST /api/auth/session/invalidate
//    kem header Cookie: JSESSIONID=... (forward tu browser)
// 2. Spring nhan request voi JSESSIONID => resolve duoc session hien tai
// 3. Session.invalidate() huy session, xoa SecurityContext
// 4. Return success => Next.js tiep tuc clear cookies phia client
//
// GET /api/auth/clear-session?redirect=...
//   Duoc PHP admin goi (qua browser redirect) truoc khi bat dau OIDC flow.
//   Dam bao khong con JSESSIONID cu (tu Next.js login) => khong bi auto-authorize
//   voi wrong user. Sau do redirect ve redirect param (thuong la /oauth2/authorize?..).

@RestController
@RequestMapping("/api/auth/session")
class SessionController {

    @PostMapping("/invalidate")
    fun invalidateSession(request: HttpServletRequest): ResponseEntity<ApiResponse<Map<String, String>>> {
        val session = request.getSession(false)
        if (session != null) {
            try {
                session.invalidate()
            } catch (e: IllegalStateException) {
                // Session da expire san => bo qua
            }
        }
        // Clear SecurityContext (best-effort, request scoped)
        SecurityContextHolder.clearContext()
        return ResponseEntity.ok(
            ApiResponse.success(
                mapOf("status" to "invalidated"),
                "Session invalidated"
            )
        )
    }

    @GetMapping("/clear-session")
    fun clearSession(
        request: HttpServletRequest,
        @RequestParam redirect: String,
    ): RedirectView {
        val session = request.getSession(false)
        if (session != null) {
            try {
                session.invalidate()
            } catch (e: IllegalStateException) {
                // Session da expire san => bo qua
            }
        }
        SecurityContextHolder.clearContext()
        return RedirectView(redirect)
    }
}
