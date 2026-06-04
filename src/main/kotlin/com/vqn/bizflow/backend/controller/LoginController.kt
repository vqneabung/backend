package com.vqn.bizflow.backend.controller

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/**
 * Controller xử lý trang login.
 *
 * Flow:
 * 1. GET /login → LoginController.login()
 *    - User đã authenticated → redirect về /redirect-dashboard (central endpoint)
 *    - User chưa auth → trả về view name "login"
 * 2. POST /login → Spring Security UsernamePasswordAuthenticationFilter xử lý
 *    (không cần controller cho POST, Security filter tự bắt)
 */
@Controller
class LoginController {

    @GetMapping("/login")
    fun login(auth: Authentication?): String {
        if (auth != null && auth.isAuthenticated) {
            return "redirect:/dispatch"
        }
        return "login"
    }
}
