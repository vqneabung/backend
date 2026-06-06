package com.vqn.bizflow.backend.storage.controller

import com.vqn.bizflow.backend.dto.ApiResponse
import com.vqn.bizflow.backend.storage.service.MinioService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * StorageController — REST API cho MinIO file operations.
 *
 * Phân quyền:
 * - Tất cả endpoints yêu cầu authentication (JWT)
 * - USER/ADMIN được upload/download/delete
 *
 * Response pattern: ApiResponse<T> (com.vqn.bizflow.backend.dto)
 */
@Tag(name = "Storage", description = "Upload / download / xóa file qua MinIO object storage")
@RestController
@RequestMapping("/api/storage")
class StorageController(
    private val minioService: MinioService,
) {
    @Operation(
        summary = "Upload file",
        description = "Upload file multipart lên MinIO. Trả về objectKey + presigned download URL.",
    )
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "Upload thành công"),
        SwaggerApiResponse(responseCode = "400", description = "File không hợp lệ"),
    ])
    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun upload(
        @RequestParam file: MultipartFile,
        @RequestParam(required = false) prefix: String?,
    ): ResponseEntity<ApiResponse<UploadResponse>> {
        require(file.size > 0) { "File is empty" }

        val originalName = file.originalFilename ?: "unknown"
        val ext = originalName.substringAfterLast('.', "").ifBlank { "bin" }
        val objectKey = buildString {
            if (!prefix.isNullOrBlank()) append("$prefix/")
            append("${UUID.randomUUID()}.$ext")
        }
        val url = minioService.upload(objectKey, file)
        return ResponseEntity.ok(
            ApiResponse.success(
                UploadResponse(
                    objectKey = objectKey,
                    url = url,
                    originalName = originalName,
                    contentType = file.contentType ?: "application/octet-stream",
                    size = file.size,
                )
            )
        )
    }

    @Operation(
        summary = "Presigned download URL",
        description = "Tạo presigned GET URL có thời hạn để client tải / hiển thị file.",
    )
    @GetMapping("/download-url")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun downloadUrl(
        @RequestParam key: String,
    ): ResponseEntity<ApiResponse<PresignedUrlResponse>> {
        val url = minioService.getPresignedUrl(key)
        return ResponseEntity.ok(
            ApiResponse.success(PresignedUrlResponse(url = url))
        )
    }

    @Operation(
        summary = "Presigned upload URL",
        description = "Tạo presigned PUT URL để client upload trực tiếp lên MinIO (dùng cho file lớn).",
    )
    @GetMapping("/upload-url")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun uploadUrl(
        @RequestParam key: String,
        @RequestParam(required = false) contentType: String?,
    ): ResponseEntity<ApiResponse<PresignedUrlResponse>> {
        val url = minioService.getPresignedUploadUrl(key, contentType)
        return ResponseEntity.ok(
            ApiResponse.success(PresignedUrlResponse(url = url))
        )
    }

    @Operation(
        summary = "Xóa file",
        description = "Xóa file khỏi MinIO bucket.",
    )
    @DeleteMapping("/files")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun delete(
        @RequestParam key: String,
    ): ResponseEntity<ApiResponse<Unit>> {
        minioService.delete(key)
        return ResponseEntity.ok(ApiResponse.ok("File deleted"))
    }

    @Operation(
        summary = "Kiểm tra file tồn tại",
        description = "Kiểm tra xem file có tồn tại trong MinIO bucket không.",
    )
    @GetMapping("/exists")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun exists(
        @RequestParam key: String,
    ): ResponseEntity<ApiResponse<Boolean>> {
        val found = minioService.exists(key)
        return ResponseEntity.ok(ApiResponse.success(found))
    }
}

// ── Response DTOs ───────────────────────────────────────────

data class UploadResponse(
    val objectKey: String,
    val url: String,
    val originalName: String,
    val contentType: String,
    val size: Long,
)

data class PresignedUrlResponse(
    val url: String,
)
