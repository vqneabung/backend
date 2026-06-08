package com.vqn.bizflow.backend.storage.service

import com.vqn.bizflow.backend.storage.config.MinioConfig.MinioProperties
import io.minio.*
import io.minio.http.Method
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.concurrent.TimeUnit

/**
 * MinioService — Wrapper xung quanh MinIO Java SDK.
 *
 * Chức năng:
 * - Upload file (multipart → MinIO)
 * - Tạo presigned download URL (GET, hết hạn theo cấu hình)
 * - Tạo presigned upload URL (PUT, cho phép client upload trực tiếp)
 * - Xóa file
 *
 * Tất cả operations đều dùng bucket từ [MinioProperties.bucket].
 */
@Service
class MinioService(
    private val minioClient: MinioClient,
    private val minioProperties: MinioProperties,
) {
    private val log = LoggerFactory.getLogger(MinioService::class.java)

    /** Bucket name lấy từ properties (mặc định: bizflow) */
    val bucket: String get() = minioProperties.bucket

    @PostConstruct
    fun ensureBucketExists() {
        log.info("MinIO configured: endpoint={}, bucket={}", minioProperties.endpoint, bucket)
        try {
            val exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucket).build()
            )
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucket).build()
                )
                log.info("Created MinIO bucket: {}", bucket)
            }
        } catch (e: Exception) {
            log.warn("Could not verify/create MinIO bucket '{}': {}", bucket, e.message)
        }
    }

    /**
     * Upload file lên MinIO, trả về presigned download URL.
     *
     * @param objectKey  Key trong bucket (VD: "products/abc-123.jpg")
     * @param file       MultipartFile từ request
     * @return Presigned GET URL (có hạn)
     */
    fun upload(objectKey: String, file: MultipartFile): String {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(objectKey)
                .stream(file.inputStream, file.size, -1)
                .contentType(file.contentType ?: "application/octet-stream")
                .build()
        )
        log.info("Uploaded {} ({}) to bucket '{}'", objectKey, file.contentType, bucket)
        return getPresignedUrl(objectKey)
    }

    /**
     * Tạo presigned GET URL (dùng để hiển thị ảnh, download file).
     */
    fun getPresignedUrl(objectKey: String): String {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .bucket(bucket)
                .`object`(objectKey)
                .method(Method.GET)
                .expiry(minioProperties.presignedUrlExpiry, TimeUnit.SECONDS)
                .build()
        )
    }

    /**
     * Tạo presigned PUT URL (dùng để client upload trực tiếp lên MinIO).
     */
    fun getPresignedUploadUrl(objectKey: String, contentType: String? = null): String {
        val args = GetPresignedObjectUrlArgs.builder()
            .bucket(bucket)
            .`object`(objectKey)
            .method(Method.PUT)
            .expiry(minioProperties.presignedUrlExpiry, TimeUnit.SECONDS)
        if (!contentType.isNullOrBlank()) {
            args.extraQueryParams(mapOf("content-type" to contentType))
        }
        return minioClient.getPresignedObjectUrl(args.build())
    }

    /**
     * Xóa file khỏi MinIO.
     */
    fun delete(objectKey: String) {
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucket)
                .`object`(objectKey)
                .build()
        )
        log.info("Deleted {} from bucket '{}'", objectKey, bucket)
    }

    /**
     * Kiểm tra file có tồn tại không.
     */
    fun exists(objectKey: String): Boolean {
        return try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(objectKey)
                    .build()
            )
            true
        } catch (e: Exception) {
            false
        }
    }
}
