package com.vqn.bizflow.backend.config

import com.vqn.bizflow.backend.dto.ApiErrorResponse
import com.vqn.bizflow.backend.dto.FieldError
import com.vqn.bizflow.backend.exception.BadRequestException
import com.vqn.bizflow.backend.exception.ConflictException
import com.vqn.bizflow.backend.exception.DuplicateException
import com.vqn.bizflow.backend.exception.ForbiddenException
import com.vqn.bizflow.backend.exception.ResourceNotFoundException
import com.vqn.bizflow.backend.exception.UnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    // ── Validation errors (400) ──

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { error ->
            FieldError(
                field = error.field,
                message = error.defaultMessage ?: "Invalid value"
            )
        }
        return ResponseEntity
            .badRequest()
            .body(ApiErrorResponse(message = "Validation failed", errors = errors))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMalformedJson(ex: HttpMessageNotReadableException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .badRequest()
            .body(ApiErrorResponse(message = "Malformed JSON request body"))
    }

    // ── Business errors ──

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(message = ex.message ?: "Resource not found"))
    }

    @ExceptionHandler(DuplicateException::class)
    fun handleDuplicate(ex: DuplicateException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse(message = ex.message ?: "Resource already exists"))
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(ex: BadRequestException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .badRequest()
            .body(ApiErrorResponse(message = ex.message ?: "Bad request"))
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(ex: UnauthorizedException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiErrorResponse(message = ex.message ?: "Unauthorized"))
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbidden(ex: ForbiddenException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiErrorResponse(message = ex.message ?: "Forbidden"))
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(ex: ConflictException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse(message = ex.message ?: "Conflict"))
    }

    // ── Spring Security errors ──

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiErrorResponse(message = "Access denied"))
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthentication(ex: AuthenticationException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiErrorResponse(message = "Authentication failed"))
    }

    // ── Optimistic locking (409) ──

    @ExceptionHandler(ObjectOptimisticLockingFailureException::class)
    fun handleOptimisticLock(ex: ObjectOptimisticLockingFailureException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse(
                message = "Dữ liệu đã bị thay đổi bởi người khác. Vui lòng tải lại và thử lại."
            ))
    }

    // ── Catch-all (500) ──

    @ExceptionHandler(Exception::class)
    fun handleUnknown(ex: Exception): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiErrorResponse(message = "Internal server error"))
    }
}