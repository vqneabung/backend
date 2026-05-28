package com.vqn.bizflow.backend.auth.service

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Service
class JwtService(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.expiration}") private val expiration: Long
) {
    private val signingKey: SecretKey by lazy {
        val keyBytes = secretKey.toByteArray()
        SecretKeySpec(keyBytes, "HmacSHA256")
    }

    fun generateToken(userId: Long, email: String, role: String): String {
        val signer = MACSigner(signingKey)

        val claimsSet = JWTClaimsSet.Builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .issueTime(Date())
            .expirationTime(Date(System.currentTimeMillis() + expiration))
            .build()

        val signedJWT = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claimsSet)
        signedJWT.sign(signer)
        return signedJWT.serialize()
    }

    fun retrieveSigningKey(): SecretKey = signingKey
}