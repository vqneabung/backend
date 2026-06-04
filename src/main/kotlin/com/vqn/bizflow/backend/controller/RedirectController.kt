package com.vqn.bizflow.backend.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.net.URI

// ===== Central Redirect Endpoint =====
// Mọi nơi cần redirect dashboard sau login đều gọi endpoint này.
// Spring Boot đọc role từ session (JSESSIONID) -> quyết định redirect URL.
//
// Tại sao không dùng /api/**?
// - ResourceServerConfig (#2) xử lý /api/** -> stateless, JWT-only -> không đọc được session
// - Endpoint này cần đọc session (JSESSIONID) -> phải thuộc FormLoginSecurityConfig (#3)
@Controller
class RedirectController(
    @Value("\${app.frontend-url}") private val frontendUrl: String,
    @Value("\${app.admin-url}") private val adminUrl: String,
) {

    @GetMapping("/dispatch")
    fun redirectDashboard(auth: Authentication?): ResponseEntity<Void> {
        if (auth == null || !auth.isAuthenticated) {
            return redirect("$frontendUrl/vi/login")
        }

        val roles = auth.authorities.map(GrantedAuthority::getAuthority)
        val targetUrl = if ("ROLE_ADMIN" in roles) {
            "$adminUrl/login?from_spring=1"
        } else {
            "$frontendUrl/vi/dashboard"
        }

        return redirect(targetUrl)
    }

    private fun redirect(url: String): ResponseEntity<Void> {
        val headers = HttpHeaders()
        headers.location = URI.create(url)
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build()
    }
}
