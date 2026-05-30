// ===== Seed OAuth2 Client =====
// Chạy sau Flyway migration (CommandLineRunner) — đảm bảo bảng oauth2_registered_client đã tồn tại
// Tạo client "nextjs-client" nếu chưa có (idempotent)
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
    // Redirect URI của Next.js — nơi nhận authorization_code từ auth server
    @Value($$"${app.oauth2.nextjs.redirect-uri}") private val redirectUri: String,
    // Nơi redirect sau khi user logout
    @Value($$"${app.oauth2.nextjs.post-logout-redirect-uri}") private val postLogoutRedirectUri: String
) : CommandLineRunner {

    override fun run(vararg args: String) {
        seedOAuth2Clients()
    }

    private fun seedOAuth2Clients() {
        // Chỉ seed nếu chưa tồn tại — tránh duplicate mỗi lần restart
        if (registeredClientRepository.findByClientId("nextjs-client") == null) {
            val nextjsClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("nextjs-client")
                // NONE = public client (không có secret). Next.js là SPA chạy trên trình duyệt
                // → secret sẽ bị lộ nếu dùng → không dùng secret, thay vào đó dùng PKCE
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                // Authorization Code flow (OAuth2/OIDC chuẩn cho web app)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                // Cho phép refresh token (access_token hết hạn 5 phút, refresh token lấy cái mới)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(redirectUri)
                .postLogoutRedirectUri(postLogoutRedirectUri)
                // OIDC scopes — openid là bắt buộc, profile cho name, email cho email claim
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .clientSettings(
                    ClientSettings.builder()
                        // PKCE bắt buộc (Proof Key for Code Exchange)
                        // Bảo vệ authorization_code không bị đánh cắp qua URL redirect
                        .requireProofKey(true)
                        // Bỏ qua consent page — nextjs-client là first-party app
                        .requireAuthorizationConsent(false)
                        .build()
                )
                .tokenSettings(
                    TokenSettings.builder()
                        // Access token chỉ sống 5 phút (ngắn = an toàn, dùng refresh token để lấy mới)
                        .accessTokenTimeToLive(Duration.ofMinutes(5))
                        // Refresh token sống 30 ngày
                        .refreshTokenTimeToLive(Duration.ofDays(30))
                        // Cho phép dùng refresh token nhiều lần (true)
                        .reuseRefreshTokens(true)
                        .build()
                )
                .build()
            registeredClientRepository.save(nextjsClient)
        }
    }
}
