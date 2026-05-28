package com.vqn.bizflow.backend.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class JwtAuthConverter : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val role = jwt.getClaimAsString("role") ?: "USER"
        val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
        return JwtAuthenticationToken(jwt, authorities)
    }
}