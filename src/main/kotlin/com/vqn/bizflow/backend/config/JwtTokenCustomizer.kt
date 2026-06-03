package com.vqn.bizflow.backend.config

import com.vqn.bizflow.backend.auth.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer

// ===== Custom Claims cho JWT =====
// Mặc định Auth Server chỉ thêm các claim chuẩn (sub, iss, aud, exp, iat, scope)
// Customizer này thêm email, role, name để resource server biết user là ai, có quyền gì
@Configuration
class JwtTokenCustomizer(
    private val userRepository: UserRepository
) {

    @Bean
    fun tokenCustomizer(): OAuth2TokenCustomizer<JwtEncodingContext> {
        return OAuth2TokenCustomizer { context ->
            // Chỉ custom access_token (không custom id_token — OIDC đã định nghĩa sẵn)
            if (context.tokenType == OAuth2TokenType.ACCESS_TOKEN) {
                // principal.name = email (vì UserDetailsServiceImpl dùng email làm username)
                val principal = context.getPrincipal<Authentication>()
                val user = userRepository.findByEmail(principal.name)
                if (user != null) {
                    // Override sub claim = user UUID thay vì email (mặc định = principal.name).
                    // OIDC spec (RFC 7519 §4.1.2): sub = locally unique identifier, never reassigned.
                    // UUID là format chuẩn — resource server parse auth.name thành UUID thành công.
                    context.claims.claim("sub", user.id.toString())
                    context.claims.claim("email", user.email)
                    context.claims.claim("role", user.role.name)
                    // name có thể null → dùng safe call + let
                    user.name?.let { context.claims.claim("name", it) }
                }
            }
        }
    }
}
