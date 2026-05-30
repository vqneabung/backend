package com.vqn.bizflow.backend.auth.service

import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.stereotype.Service
import java.time.Instant

// ===== JWT Service cho API Login =====
// Dùng cho POST /api/auth/login và POST /api/auth/register
// Sử dụng CHUNG RSA key với Authorization Server (JWKSource)
// → Tất cả JWT đều ký RS256 → 1 JwtDecoder verify được tất cả
@Service
class JwtService(
    // Dùng chung RSA key với Auth Server (thay vì HS256 + shared secret như cũ)
    private val jwkSource: JWKSource<SecurityContext>,
    @Value("\${jwt.expiration}") private val expiration: Long
) {
    fun generateToken(userId: Long, email: String, role: String): String {
        // NimbusJwtEncoder tự động chọn RSA key từ JWKSource để ký JWT
        val encoder = NimbusJwtEncoder(jwkSource)
        val claims = JwtClaimsSet.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .issuedAt(Instant.now())
            // expiresAt = now + expiration (config: jwt.expiration, default 24h)
            .expiresAt(Instant.now().plusMillis(expiration))
            .build()
        return encoder.encode(JwtEncoderParameters.from(claims)).tokenValue
    }
}