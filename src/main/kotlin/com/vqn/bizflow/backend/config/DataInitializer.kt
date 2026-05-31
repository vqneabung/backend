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
    // Secret dùng để Next.js xác thực khi exchange authorization code lấy token
    // Lấy từ biến môi trường NEXTJS_CLIENT_SECRET (default: nextjs-secret)
    @Value("\${app.oauth2.nextjs.client-secret}") private val clientSecret: String,
    // Redirect URI của Next.js — nơi nhận authorization_code từ auth server
    @Value("\${app.oauth2.nextjs.redirect-uri}") private val redirectUri: String,
    // Nơi redirect sau khi user logout
    @Value("\${app.oauth2.nextjs.post-logout-redirect-uri}") private val postLogoutRedirectUri: String
) : CommandLineRunner {

    override fun run(vararg args: String) {
        seedOAuth2Clients()
    }

    private fun seedOAuth2Clients() {
        // Chỉ seed nếu chưa tồn tại — tránh duplicate mỗi lần restart
        if (registeredClientRepository.findByClientId("nextjs-client") == null) {
            val nextjsClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("nextjs-client")
                // Dùng client_secret để Next.js (BFF) xác thực với token endpoint
                // {noop} = plain text (không mã hóa trong DB — môi trường dev)
                // Production: dùng {bcrypt} thay {noop}
                .clientSecret("{noop}$clientSecret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
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
                        // PKCE (Proof Key for Code Exchange): bảo vệ authorization_code khỏi bị
                        // đánh cắp trên URL redirect. Dù Next.js là BFF server-side,
                        // code vẫn đi qua browser URL → PKCE thêm 1 lớp bảo vệ.
                        // OAuth 2.1 khuyến nghị PKCE cho ALL clients.
                        // Dùng đồng thời client_secret + PKCE = defense in depth.
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
