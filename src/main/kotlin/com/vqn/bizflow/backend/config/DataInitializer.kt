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

@Component
class DataInitializer(
    private val registeredClientRepository: RegisteredClientRepository,
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
    }

    /** Client cho Next.js (user app) — redirect về /api/auth/callback/oidc */
    private fun seedNextjsClient() {
        if (registeredClientRepository.findByClientId("nextjs-client") != null) return
        saveClient(
            clientId = "nextjs-client",
            clientSecret = nextjsSecret,
            redirectUri = nextjsRedirectUri
        )
    }

    /** Client cho Laravel Admin — redirect về /admin/callback */
    private fun seedLaravelAdminClient() {
        if (registeredClientRepository.findByClientId("laravel-admin-client") != null) return
        saveClient(
            clientId = "laravel-admin-client",
            clientSecret = laravelSecret,
            redirectUri = laravelRedirectUri
        )
    }

    /** Helper — tạo RegisteredClient với config chung */
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
                // PKCE bắt buộc — defense in depth cùng với client_secret
                .requireProofKey(true)
                // Bỏ qua consent page — first-party apps
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
