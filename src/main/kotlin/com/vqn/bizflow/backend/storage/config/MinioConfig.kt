package com.vqn.bizflow.backend.storage.config

import io.minio.MinioClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * MinioConfig — Cấu hình MinIO Client.
 *
 * Đọc properties từ prefix "minio.*" (application.properties / env).
 * Tự động tạo bucket nếu chưa tồn tại.
 */
@Configuration
@EnableConfigurationProperties(MinioConfig.MinioProperties::class)
class MinioConfig {

    @Bean
    fun minioClient(properties: MinioProperties): MinioClient =
        MinioClient.builder()
            .endpoint(properties.endpoint)
            .credentials(properties.accessKey, properties.secretKey)
            .build()

    @ConfigurationProperties(prefix = "minio")
    data class MinioProperties(
        var endpoint: String = "http://localhost:9000",
        var accessKey: String = "minioadmin",
        var secretKey: String = "minioadmin",
        var bucket: String = "bizflow",
        var presignedUrlExpiry: Int = 3600,
    )
}
