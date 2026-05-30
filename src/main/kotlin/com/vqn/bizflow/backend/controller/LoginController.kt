package com.vqn.bizflow.backend.controller

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
 * 1. GET /login → LoginController.login() → trả về view name "login"
 * 2. ThymeleafViewResolver resolve "login" → templates/login.html
 * 3. POST /login → Spring Security UsernamePasswordAuthenticationFilter xử lý
 *    (không cần controller cho POST, Security filter tự bắt)
 */
@Controller
class LoginController {

    /**
     * Hiển thị trang đăng nhập.
     * Trả về view name "login" → Thymeleaf render templates/login.html
     */
    @GetMapping("/login")
    fun login(): String = "login"
}
