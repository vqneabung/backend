package com.vqn.bizflow.backend.config

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
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
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.UUID

@Configuration
class AuthorizationServerConfig(
    // Issuer URL từ config (VD: http://localhost:8080)
    // Spring Boot đọc từ spring.security.oauth2.authorizationserver.issuer
    @Value("\${spring.security.oauth2.authorizationserver.issuer}") private val issuer: String,

    // Đường dẫn lưu RSA key pair (PEM format). Mặc định: ./jwt-keypair.pem
    // Set qua env: JWT_KEY_FILE=/var/secrets/jwt-keypair.pem
    @Value("\${bizflow.jwt.key-file:\${JWT_KEY_FILE:./jwt-keypair.pem}}") private val keyFilePath: String
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
    //
    // Key pair được PERSIST xuống file PEM (mặc định ./jwt-keypair.pem) để:
    // 1. Không bị invalid JWT sau mỗi lần restart
    // 2. Đảm bảo signature verification thành công cho tất cả access_token đang lưu hành
    // 3. Cho phép chia sẻ key giữa nhiều instance qua shared volume
    @Bean
    fun jwkSource(): JWKSource<SecurityContext> {
        val keyPair = loadOrCreateRsaKey()
        val publicKey = keyPair.public as RSAPublicKey
        val privateKey = keyPair.private as RSAPrivateKey
        val rsaKey = RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .algorithm(JWSAlgorithm.RS256)
            .build()
        val jwkSet = JWKSet(rsaKey)
        return ImmutableJWKSet(jwkSet)
    }

    /**
     * Load RSA key pair từ file PEM. Nếu file không tồn tại → generate mới và save.
     * Format PEM tự định nghĩa: cả private key (PKCS#8) + public key (X.509) trong cùng file.
     *
     * Format file:
     *   -----BEGIN PRIVATE KEY-----
     *   <base64 PKCS#8>
     *   -----END PRIVATE KEY-----
     *   -----BEGIN PUBLIC KEY-----
     *   <base64 X.509>
     *   -----END PUBLIC KEY-----
     */
    private fun loadOrCreateRsaKey(): KeyPair {
        val path: Path = Paths.get(keyFilePath)
        val file = path.toFile()
        return try {
            if (file.exists() && file.length() > 0) {
                val pem = file.readText(Charsets.UTF_8)
                val privateKey = readPrivateKeyFromPem(pem)
                val publicKey = readPublicKeyFromPem(pem)
                KeyPair(publicKey, privateKey)
            } else {
                // Tạo key pair mới
                val newPair = generateRsaKey()
                // Tạo thư mục cha nếu chưa có
                file.parentFile?.mkdirs()
                file.writeText(toPem(newPair), Charsets.UTF_8)
                // Set file permissions chỉ owner đọc được (chmod 600) — best effort trên POSIX
                try {
                    val setOwnerOnly = file.setReadable(false, false) and file.setReadable(true, true)
                    val setWriteOwner = file.setWritable(false, false) and file.setWritable(true, true)
                    if (!setOwnerOnly || !setWriteOwner) {
                        // Trên Windows hoặc FS không hỗ trợ → bỏ qua
                    }
                } catch (_: Exception) {
                    // best-effort
                }
                newPair
            }
        } catch (ex: Exception) {
            // Nếu file lỗi (corrupt, format sai) → log + sinh lại để không crash app
            System.err.println("[WARN] Failed to load RSA key from $keyFilePath: ${ex.message}. Generating new key.")
            val newPair = generateRsaKey()
            try {
                file.writeText(toPem(newPair), Charsets.UTF_8)
            } catch (_: Exception) {
                // Nếu không ghi được file thì cứ trả về key trong memory, lần sau sẽ retry
            }
            newPair
        }
    }

    private fun generateRsaKey(): KeyPair {
        return try {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(2048)
            keyPairGenerator.generateKeyPair()
        } catch (ex: Exception) {
            throw IllegalStateException("Failed to generate RSA key", ex)
        }
    }

    /** Convert KeyPair sang PEM format (PKCS#8 private + X.509 public). */
    private fun toPem(keyPair: KeyPair): String {
        val privateKeyBase64 = Base64.getMimeEncoder(64, "\n".toByteArray())
            .encodeToString(keyPair.private.encoded)
        val publicKeyBase64 = Base64.getMimeEncoder(64, "\n".toByteArray())
            .encodeToString(keyPair.public.encoded)
        return buildString {
            append("-----BEGIN PRIVATE KEY-----\n")
            append(privateKeyBase64)
            append("\n-----END PRIVATE KEY-----\n")
            append("-----BEGIN PUBLIC KEY-----\n")
            append(publicKeyBase64)
            append("\n-----END PUBLIC KEY-----\n")
        }
    }

    private fun readPrivateKeyFromPem(pem: String): PrivateKey {
        // Tách đoạn PKCS#8 (phần đầu tiên trước marker public key)
        val pkcs8Part = pem.substringAfter("-----BEGIN PRIVATE KEY-----")
            .substringBefore("-----END PRIVATE KEY-----")
            .replace(Regex("\\s+"), "")
        val pkcs8Bytes = Base64.getDecoder().decode(pkcs8Part)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(PKCS8EncodedKeySpec(pkcs8Bytes))
    }

    private fun readPublicKeyFromPem(pem: String): PublicKey {
        val x509Part = pem.substringAfter("-----BEGIN PUBLIC KEY-----")
            .substringBefore("-----END PUBLIC KEY-----")
            .replace(Regex("\\s+"), "")
        val x509Bytes = Base64.getDecoder().decode(x509Part)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(X509EncodedKeySpec(x509Bytes))
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
