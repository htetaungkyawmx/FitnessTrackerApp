package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("error")
    val error: ErrorResponse? = null
) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = true,
                message = message,
                data = data,
                error = null
            )
        }

        fun <T> error(errorMessage: String, errorCode: Int? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = errorMessage,
                data = null,
                error = ErrorResponse(
                    code = errorCode ?: 400,
                    message = errorMessage
                )
            )
        }

        fun <T> fromException(exception: Exception): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = exception.message ?: "Unknown error occurred",
                data = null,
                error = ErrorResponse(
                    code = 500,
                    message = exception.message ?: "Internal server error"
                )
            )
        }
    }
}

data class ErrorResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("details")
    val details: Map<String, String>? = null
)

data class PaginatedResponse<T>(
    @SerializedName("items")
    val items: List<T>,

    @SerializedName("pagination")
    val pagination: Pagination
)

data class Pagination(
    @SerializedName("current_page")
    val currentPage: Int,

    @SerializedName("total_pages")
    val totalPages: Int,

    @SerializedName("total_items")
    val totalItems: Int,

    @SerializedName("items_per_page")
    val itemsPerPage: Int,

    @SerializedName("has_next")
    val hasNext: Boolean,

    @SerializedName("has_previous")
    val hasPrevious: Boolean
)