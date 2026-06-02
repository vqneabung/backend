package com.vqn.bizflow.backend.controller

import com.vqn.bizflow.backend.util.SecurityUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/**
 * Controller xử lý trang login.
 *
 * Tại sao dùng @Controller thay vì ViewControllerRegistry trong FormLoginSecurityConfig?
 * - ViewController được đăng ký qua WebMvcConfigurer bean bên trong @Configuration class
 *   có thể không hoạt động đúng khi kết hợp với Spring Security formLogin().loginPage()
 *   trong Spring Security 7.x + Spring Boot 4.x.
 * - Dùng @Controller riêng biệt là cách đáng tin cậy nhất để render Thymeleaf template.
 *
 * Flow:
 * 1. GET /login → LoginController.login()
 *    - User đã authenticated → redirect về frontend theo role
 *    - User chưa auth → trả về view name "login"
 * 2. POST /login → Spring Security UsernamePasswordAuthenticationFilter xử lý
 *    (không cần controller cho POST, Security filter tự bắt)
 */
@Controller
class LoginController(
    @Value("\${app.frontend-url}") private val frontendUrl: String,
    @Value("\${app.admin-url}") private val adminUrl: String,
) {

    /**
     * Hiển thị trang đăng nhập, hoặc redirect về frontend nếu đã login.
     */
    @GetMapping("/login")
    fun login(auth: Authentication?): String {
        // Đã login → redirect về dashboard theo role
        if (auth != null && auth.isAuthenticated) {
            return "redirect:${SecurityUtils.determineRedirectUrl(auth, frontendUrl, adminUrl)}"
        }
        // Chưa login → render form login
        return "login"
    }
}
