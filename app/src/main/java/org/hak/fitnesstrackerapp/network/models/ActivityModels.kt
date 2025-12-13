package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName

data class ActivityResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("activity_type") val activityType: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("duration_minutes") val durationMinutes: Int,
    @SerializedName("distance_km") val distanceKm: Double?,
    @SerializedName("calories_burned") val caloriesBurned: Int?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("created_at") val createdAt: String
)

data class ActivitiesResponse(
    @SerializedName("activities") val activities: List<ActivityResponse>,
    @SerializedName("statistics") val statistics: StatisticsResponse
)

data class StatisticsResponse(
    @SerializedName("total_activities") val totalActivities: Int,
    @SerializedName("total_minutes") val totalMinutes: Int,
    @SerializedName("total_calories") val totalCalories: Int,
    @SerializedName("total_distance") val totalDistance: Double?,
    @SerializedName("average_duration") val averageDuration: Double?
)