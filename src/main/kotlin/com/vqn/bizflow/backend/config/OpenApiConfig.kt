package com.vqn.bizflow.backend.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenApi(): OpenAPI = OpenAPI()
        .info(Info()
            .title("Bizflow API")
            .description("REST API for Bizflow Digital Transformation Platform")
            .version("0.0.1"))
        .components(Components()
            .addSecuritySchemes("bearer-jwt", SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Nhập JWT token nhận được từ /api/auth/login hoặc /api/auth/register")))
}