package com.vqn.bizflow.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

/**
 * DataInitializer — Seed dữ liệu mặc định khi app khởi chạy.
 *
 * Các dữ liệu được seed:
 * 1. OAuth2 clients (Next.js + Laravel Admin) — upsert: update nếu đã tồn tại
 * 2. Users mặc định (Owner + Admin)
 *
 * Upsert giúp các config như clientAuthenticationMethod luôn được cập nhật
 * khi code thay đổi, không cần drop DB mỗi lần.
 */
@Component
class DataInitializer(
    private val registeredClientRepository: RegisteredClientRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userSeedService: UserSeedService,
    // Secret cho Next.js
    @Value("\${app.oauth2.nextjs.client-secret}") private val nextjsSecret: String,
    // Redirect URI của Next.js
    @Value("\${app.oauth2.nextjs.redirect-uri}") private val nextjsRedirectUri: String,
    // Secret cho Laravel Admin
    @Value("\${app.oauth2.laravel.client-secret}") private val laravelSecret: String,
    // Redirect URI của Laravel Admin
    @Value("\${app.oauth2.laravel.redirect-uri}") private val laravelRedirectUri: String,
) : CommandLineRunner {

    override fun run(vararg args: String) {
        seedClient("nextjs-client", nextjsSecret, nextjsRedirectUri)
        seedClient("laravel-admin-client", laravelSecret, laravelRedirectUri)
        userSeedService.seedIfEmpty()
    }

    // ===== OAuth2 Clients — Upsert: update nếu tồn tại, insert nếu chưa =====

    /**
     * Upsert OAuth2 client:
     * - Nếu clientId đã tồn tại → update clientAuthenticationMethod + clientSecret + tokenSettings
     * - Nếu chưa tồn tại → tạo mới với đầy đủ config
     *
     * Lý do cập nhật tokenSettings khi update: TTL access_token/refresh_token
     * có thể thay đổi theo code, cần áp dụng ngay cho client cũ. Nếu không
     * upsert, client cũ giữ TTL cũ vĩnh viễn → inconsistent giữa các môi trường.
     */
    private fun seedClient(clientId: String, clientSecret: String, redirectUri: String) {
        val existing = registeredClientRepository.findByClientId(clientId)
        if (existing != null) {
            val updated = RegisteredClient.from(existing)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .clientSecret(passwordEncoder.encode(clientSecret))
                .clientName(clientId)
                .tokenSettings(defaultTokenSettings())
                .build()
            registeredClientRepository.save(updated)
            return
        }
        saveClient(clientId, clientSecret, redirectUri)
    }

    /**
     * Token settings thống nhất cho cả OIDC flow và AuthService.login():
     * - accessToken TTL: 24h (khớp với app.jwt.expiration trong application.properties)
     * - refreshToken TTL: 30d
     * - Reuse refresh token (giảm số lượng refresh token được issue)
     *
     * Trước đây TTL là 5 phút → user bị logout ngầm sau mỗi 5 phút, gây UX kém.
     * Đã nâng lên 24h để khớp với TTL của AuthService.login().
     */
    private fun defaultTokenSettings(): TokenSettings {
        return TokenSettings.builder()
            .accessTokenTimeToLive(Duration.ofHours(24))
            .refreshTokenTimeToLive(Duration.ofDays(30))
            .reuseRefreshTokens(true)
            .build()
    }

    private fun saveClient(clientId: String, clientSecret: String, redirectUri: String) {
        val client = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(clientId)
            .clientName(clientId)
            .clientSecret(passwordEncoder.encode(clientSecret))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri(redirectUri)
            .scope(OidcScopes.OPENID)
            .scope(OidcScopes.PROFILE)
            .scope(OidcScopes.EMAIL)
            .clientSettings(ClientSettings.builder()
                .requireProofKey(true)
                .requireAuthorizationConsent(false)
                .build())
            .tokenSettings(defaultTokenSettings())
            .build()
        registeredClientRepository.save(client)
    }
}
