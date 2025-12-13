package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName

data class PaginatedActivitiesResponse(
    @SerializedName("activities")
    val activities: List<Activity>,

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