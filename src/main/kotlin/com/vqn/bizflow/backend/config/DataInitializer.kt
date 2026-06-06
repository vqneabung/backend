package com.vqn.bizflow.backend.config

import com.vqn.bizflow.backend.product.entity.CategoryEntity
import com.vqn.bizflow.backend.product.entity.UnitEntity
import com.vqn.bizflow.backend.product.repository.CategoryRepository
import com.vqn.bizflow.backend.product.repository.UnitRepository
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
 * 3. Global units (Bao, Kg, Thùng, Cái...)
 * 4. Global categories (VLXD, Tạp hóa, Điện nước...)
 */
@Component
class DataInitializer(
    private val registeredClientRepository: RegisteredClientRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userSeedService: UserSeedService,
    private val unitRepository: UnitRepository,
    private val categoryRepository: CategoryRepository,
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
        seedGlobalUnits()
        seedGlobalCategories()
    }

    // ===== OAuth2 Clients =====

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

    // ===== Global Reference Data =====

    /**
     * Seed global units (ownerId = null).
     * Chỉ tạo nếu chưa có — kiểm tra bằng count.
     */
    private fun seedGlobalUnits() {
        if (unitRepository.count() > 0) return

        val globalUnits = listOf(
            "Bao", "Kg", "Thùng", "Cái", "Mét",
            "Lít", "Chai", "Hộp", "Tấn", "Gram",
        )
        globalUnits.forEach { name ->
            unitRepository.save(UnitEntity(name = name, description = "Đơn vị toàn cục"))
        }
    }

    /**
     * Seed global categories (ownerId = null).
     * Chỉ tạo nếu chưa có.
     */
    private fun seedGlobalCategories() {
        if (categoryRepository.count() > 0) return

        val globalCategories = listOf(
            "VLXD" to "Vật liệu xây dựng",
            "Tạp hóa" to "Hàng tạp hóa",
            "Điện nước" to "Thiết bị điện nước",
            "Sắt thép" to "Sắt thép xây dựng",
            "Sơn" to "Sơn và phụ kiện sơn",
            "Ống nước" to "Ống nước & phụ kiện",
            "Gạch" to "Gạch xây / gạch ốp lát",
            "Xi măng" to "Xi măng các loại",
        )
        globalCategories.forEach { (name, desc) ->
            categoryRepository.save(CategoryEntity(name = name, description = desc))
        }
    }
}
