package com.vqn.bizflow.backend.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Field-level validation error")
data class FieldError(
    @field:Schema(description = "Field name", example = "email")
    val field: String,

    @field:Schema(description = "Error message", example = "Email must be valid")
    val message: String
)

@Schema(description = "Standard API error response")
data class ApiErrorResponse(
    @field:Schema(description = "Indicates if the request was successful", example = "false")
    val success: Boolean = false,

    @field:Schema(description = "Error message", example = "Validation failed")
    val message: String,

    @field:Schema(description = "List of field-level errors (only for validation errors)")
    val errors: List<FieldError>? = null
)