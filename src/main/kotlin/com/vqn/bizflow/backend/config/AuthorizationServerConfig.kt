package com.vqn.bizflow.backend.config

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import java.util.UUID

@Configuration
class AuthorizationServerConfig(
    // Issuer URL từ config (VD: http://localhost:8080)
    // Spring Boot đọc từ spring.security.oauth2.authorizationserver.issuer
    @Value($$"${spring.security.oauth2.authorizationserver.issuer}") private val issuer: String
) {

    // ===== Security Filter Chain #1: Authorization Server Endpoints =====
    // Order(1) — chạy đầu tiên, chỉ bắt các URL OAuth2 (/oauth2/*, /.well-known/*)
    // Khi user chưa login → redirect đến /login (form login page)
    @Bean
    @Order(1)
    fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.oauth2AuthorizationServer { authServer ->
            // Chỉ chain này xử lý OAuth2 endpoints (authorize, token, jwks...)
            // Nếu thiếu securityMatcher, chain này bắt TẤT CẢ request → các chain khác không chạy
            http.securityMatcher(authServer.endpointsMatcher)
                // Bật OpenID Connect 1.0 — cho phép /.well-known/openid-configuration, id_token, userinfo
            authServer.oidc(Customizer.withDefaults())
        }
        http.authorizeHttpRequests { auth ->
            auth.anyRequest().authenticated()
        }
        // Phân biệt request từ browser vs API:
        // - Browser (Accept: text/html) → redirect đến /login form
        // - API (Accept: application/json) → trả 401 JSON (không redirect)
        http.exceptionHandling { exceptions ->
            exceptions.defaultAuthenticationEntryPointFor(
                LoginUrlAuthenticationEntryPoint("/login"),
                MediaTypeRequestMatcher(MediaType.TEXT_HTML)
            )
        }
        return http.build()
    }
  
    // ===== Client Registry: Lưu danh sách app được phép dùng Auth Server =====
    // Sử dụng JDBC (không InMemory) để client không bị mất khi restart server
    // Dữ liệu lưu ở bảng oauth2_registered_client
    @Bean
    fun registeredClientRepository(jdbcTemplate: JdbcTemplate): RegisteredClientRepository {
        return JdbcRegisteredClientRepository(jdbcTemplate)
    }

    // ===== Authorization Service: Lưu trạng thái của các OAuth2 authorization =====
    // Lưu authorization_code, access_token, refresh_token, id_token vào DB
    // Nếu dùng InMemory: restart server → token giữa chừng bị mất → user phải login lại
    @Bean
    fun authorizationService(jdbcTemplate: JdbcTemplate, registeredClientRepository: RegisteredClientRepository): OAuth2AuthorizationService {
        return JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository)
    }

    // ===== Consent Service: Lưu quyết định "cho phép" của user với client =====
    // Hiện tại auto-approve (nextjs-client là first-party), nhưng vẫn tạo bean cho đủ
    @Bean
    fun authorizationConsentService(jdbcTemplate: JdbcTemplate, registeredClientRepository: RegisteredClientRepository): OAuth2AuthorizationConsentService {
        return JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository)
    }

    // ===== JWK Source: Cung cấp RSA key pair để ký JWT =====
    // - Private key: dùng để KÝ access_token, id_token, refresh_token
    // - Public key: publish ở GET /oauth2/jwks để resource server (Laravel, API) VERIFY chữ ký
    // Mỗi key có keyID riêng (UUID) để resource server biết dùng key nào verify
    @Bean
    fun jwkSource(): JWKSource<SecurityContext> {
        val keyPair = generateRsaKey()
        val publicKey = keyPair.public as RSAPublicKey
        val privateKey = keyPair.private as RSAPrivateKey
        val rsaKey = RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .build()
        val jwkSet = JWKSet(rsaKey)
        return ImmutableJWKSet(jwkSet)
    }

    // Sinh RSA 2048-bit key pair (đủ mạnh cho production)
    // ⚠️ Deploy thật: nên lưu key vào file .jks để persist giữa các lần restart
    private fun generateRsaKey(): KeyPair {
        return try {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(2048)
            keyPairGenerator.generateKeyPair()
        } catch (ex: Exception) {
            throw IllegalStateException(ex)
        }
    }

    // ===== JWT Decoder: Decode và verify JWT =====
    // Dùng RSA public key từ JWKSource để kiểm tra chữ ký của token
    // Bean này được CẢ Auth Server và Resource Server dùng chung
    @Bean
    fun jwtDecoder(jwkSource: JWKSource<SecurityContext>): JwtDecoder {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource)
    }

    // ===== Authorization Server Settings =====
    // issuer: URL của auth server (xuất hiện trong iss claim của JWT + /.well-known/openid-configuration)
    @Bean
    fun authorizationServerSettings(): AuthorizationServerSettings {
        return AuthorizationServerSettings.builder()
            .issuer(issuer)
            .build()
    }
}
