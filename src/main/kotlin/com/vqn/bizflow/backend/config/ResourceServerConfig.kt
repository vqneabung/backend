package com.vqn.bizflow.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

// ===== Security Filter Chain #2: Resource Server (API) =====
// Xử lý tất cả request /api/** — yêu cầu JWT Bearer token trong header Authorization
// Stateless: không dùng session, mỗi request phải gửi token
//
// Lưu ý: KHÔNG dùng @EnableWebSecurity ở đây — Spring Boot đã auto-configure
// security filter chain. Có annotation này có thể gây conflict với ViewResolver
// và các auto-configuration khác của Spring MVC.
@Configuration
@EnableMethodSecurity
class ResourceServerConfig(
    private val authEntryPoint: CustomAuthenticationEntryPoint,
    private val accessDeniedHandler: CustomAccessDeniedHandler,
    private val jwtAuthConverter: JwtAuthConverter
) {

    @Bean
    @Order(2)
    fun resourceServerFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            // Chỉ chain này xử lý URL bắt đầu bằng /api/ (các URL khác pass qua)
            .securityMatcher("/api/**")
            // API stateless không cần CSRF (CSRF chỉ cần cho form login session-based)
            .csrf { it.disable() }
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint(authEntryPoint)   // 401 nếu không có token
                ex.accessDeniedHandler(accessDeniedHandler)   // 403 nếu token không đủ quyền
            }
            // Yêu cầu JWT trong header Authorization: Bearer <token>
            // JwtDecoder bean (từ AuthorizationServerConfig) tự động được dùng để verify token
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    // Extract claim "role" từ JWT → tạo ROLE_USER / ROLE_ADMIN authority
                    jwt.jwtAuthenticationConverter(jwtAuthConverter)
                }
                oauth2.authenticationEntryPoint(authEntryPoint)
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**").permitAll()   // login/register không cần token
                    .requestMatchers("/scalar/**").permitAll()     // API docs UI
                    .requestMatchers("/v3/api-docs/**").permitAll() // OpenAPI spec
                    .anyRequest().authenticated()
            }
            // Stateless = không tạo session cho API. Mỗi request verify JWT độc lập
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .build()
    }

    // Bean dùng chung: mã hóa password với BCrypt (mạnh, có salt tự động)
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }
}
