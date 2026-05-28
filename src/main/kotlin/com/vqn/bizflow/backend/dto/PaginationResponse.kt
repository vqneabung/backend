package com.vqn.bizflow.backend.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Pagination metadata")
data class PaginationMeta(
    @field:Schema(description = "Current page index (0-based)", example = "0")
    val page: Int,

    @field:Schema(description = "Page size", example = "20")
    val size: Int,

    @field:Schema(description = "Total number of elements", example = "150")
    val totalElements: Long,

    @field:Schema(description = "Total number of pages", example = "8")
    val totalPages: Int
)

@Schema(description = "Standard paginated response wrapper")
data class PaginationResponse<T>(
    @field:Schema(description = "Indicates if the request was successful", example = "true")
    val success: Boolean = true,

    @field:Schema(description = "Response message", example = "Success")
    val message: String = "Success",

    @field:Schema(description = "List of items")
    val data: List<T>,

    @field:Schema(description = "Pagination metadata")
    val pagination: PaginationMeta
) {
    companion object {
        fun <T> of(
            data: List<T>,
            page: Int,
            size: Int,
            totalElements: Long
        ): PaginationResponse<T> {
            val totalPages = if (size > 0) kotlin.math.ceil(totalElements.toDouble() / size).toInt() else 0
            return PaginationResponse(
                data = data,
                pagination = PaginationMeta(
                    page = page,
                    size = size,
                    totalElements = totalElements,
                    totalPages = totalPages
                )
            )
        }
    }
}