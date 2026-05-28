package com.vqn.bizflow.backend.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Standard API response wrapper")
data class ApiResponse<T>(
    @field:Schema(description = "Indicates if the request was successful", example = "true")
    val success: Boolean = true,

    @field:Schema(description = "Response message", example = "Login successful")
    val message: String = "Success",

    @field:Schema(description = "Response payload")
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T, message: String = "Success"): ApiResponse<T> =
            ApiResponse(success = true, message = message, data = data)

        fun <T> created(data: T, message: String = "Created"): ApiResponse<T> =
            ApiResponse(success = true, message = message, data = data)

        fun <T> ok(message: String = "Success"): ApiResponse<T> =
            ApiResponse(success = true, message = message, data = null)
    }
}