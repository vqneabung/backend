package com.vqn.bizflow.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

// ===== Security Filter Chain #3: Form Login (Default) =====
// Xử lý các URL còn lại (không phải OAuth2, không phải /api/**)
// Quan trọng: chain này render và xử lý /login page
// Sau khi user login thành công → tạo session → redirect về URL gốc
//
// Lưu ý: GET /login được xử lý bởi LoginController.kt (không dùng ViewControllerRegistry)
// vì ViewController trong WebMvcConfigurer bean có thể không hoạt động đúng với
// Spring Security 7.x + Spring Boot 4.x khi kết hợp formLogin().loginPage()
@Configuration
class FormLoginSecurityConfig {

    @Bean
    @Order(3)
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/login", "/error").permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin { form ->
                // Dùng custom login page (Thymeleaf) thay vì default Spring Security form
                form.loginPage("/login").permitAll()
            }
        return http.build()
    }
}
