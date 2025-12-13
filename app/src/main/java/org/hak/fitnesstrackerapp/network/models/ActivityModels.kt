package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName

data class ActivityRequest(
    @SerializedName("type")
    val type: String,

    @SerializedName("duration_minutes")
    val durationMinutes: Int,

    @SerializedName("distance_km")
    val distanceKm: Double? = null,

    @SerializedName("calories_burned")
    val caloriesBurned: Int? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("start_time")
    val startTime: String? = null,

    @SerializedName("end_time")
    val endTime: String? = null,

    @SerializedName("location")
    val location: ActivityLocationRequest? = null  // Changed name
)

// Renamed to avoid conflict
data class ActivityLocationRequest(
    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("altitude")
    val altitude: Double? = null,

    @SerializedName("accuracy")
    val accuracy: Float? = null
)

data class ActivityResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("type")
    val type: String,

    @SerializedName("display_name")
    val displayName: String,

    @SerializedName("duration_minutes")
    val durationMinutes: Int,

    @SerializedName("distance_km")
    val distanceKm: Double?,

    @SerializedName("calories_burned")
    val caloriesBurned: Int?,

    @SerializedName("notes")
    val notes: String?,

    @SerializedName("start_time")
    val startTime: String,

    @SerializedName("end_time")
    val endTime: String,

    @SerializedName("location")
    val location: ActivityLocationResponse?,  // Changed name

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

// Renamed to avoid conflict
data class ActivityLocationResponse(
    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("altitude")
    val altitude: Double?,

    @SerializedName("accuracy")
    val accuracy: Float?
)

data class ActivitiesResponse(
    @SerializedName("activities")
    val activities: List<ActivityResponse>,

    @SerializedName("statistics")
    val statistics: StatisticsResponse,

    @SerializedName("pagination")
    val pagination: Pagination? = null
)

data class ActivitySummaryResponse(
    @SerializedName("date")
    val date: String,

    @SerializedName("total_activities")
    val totalActivities: Int,

    @SerializedName("total_duration")
    val totalDuration: Int,

    @SerializedName("total_calories")
    val totalCalories: Int,

    @SerializedName("total_distance")
    val totalDistance: Double,

    @SerializedName("activities_by_type")
    val activitiesByType: Map<String, Int>
)

data class ActivityStatsResponse(
    @SerializedName("period")
    val period: String,

    @SerializedName("total_activities")
    val totalActivities: Int,

    @SerializedName("total_duration")
    val totalDuration: Int,

    @SerializedName("total_calories")
    val totalCalories: Int,

    @SerializedName("total_distance")
    val totalDistance: Double,

    @SerializedName("average_duration")
    val averageDuration: Double,

    @SerializedName("most_common_activity")
    val mostCommonActivity: String?,

    @SerializedName("streak")
    val streak: Int,

    @SerializedName("daily_averages")
    val dailyAverages: Map<String, Double>
)