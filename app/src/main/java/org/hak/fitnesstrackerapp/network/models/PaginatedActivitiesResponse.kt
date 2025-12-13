package org.hak.fitnesstrackerapp.network.models

data class PaginatedActivitiesResponse(
    val activities: List<ActivityResponse>,
    val pagination: Pagination
)