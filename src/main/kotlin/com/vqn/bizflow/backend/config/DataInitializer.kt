package com.vqn.bizflow.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
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
 * DataInitializer — Seed dữ liệu mặc định khi app khởi chạy lần đầu.
 *
 * Các dữ liệu được seed:
 * 1. OAuth2 clients (Next.js + Laravel Admin)
 * 2. Users mặc định (Owner + Admin)
 *
 * Tất cả đều kiểm tra tồn tại trước — nếu có rồi thì skip.
 */
@Component
class DataInitializer(
    private val registeredClientRepository: RegisteredClientRepository,
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
        seedNextjsClient()
        seedLaravelAdminClient()
        userSeedService.seedIfEmpty()
    }

    // ===== OAuth2 Clients =====

    private fun seedNextjsClient() {
        if (registeredClientRepository.findByClientId("nextjs-client") != null) return
        saveClient("nextjs-client", nextjsSecret, nextjsRedirectUri)
    }

    private fun seedLaravelAdminClient() {
        if (registeredClientRepository.findByClientId("laravel-admin-client") != null) return
        saveClient("laravel-admin-client", laravelSecret, laravelRedirectUri)
    }

    private fun saveClient(clientId: String, clientSecret: String, redirectUri: String) {
        val client = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(clientId)
            .clientSecret("{noop}$clientSecret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
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
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(5))
                .refreshTokenTimeToLive(Duration.ofDays(30))
                .reuseRefreshTokens(true)
                .build())
            .build()
        registeredClientRepository.save(client)
    }
}
