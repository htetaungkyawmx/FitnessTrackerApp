package org.hak.fitnesstrackerapp.network.models

data class BaseResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val error: ErrorResponse? = null
)

data class ErrorResponse(
    val code: Int,
    val message: String,
    val details: Map<String, String>? = null
)

data class PaginatedResponse<T>(
    val items: List<T>,
    val pagination: Pagination
)

data class Pagination(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val itemsPerPage: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)