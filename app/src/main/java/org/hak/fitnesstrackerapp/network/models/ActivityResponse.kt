package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName

data class ActivityIdResponse(
    @SerializedName("activity_id")
    val activityId: Int
)

data class ActivityResponse(
    @SerializedName("activities")
    val activities: List<Activity>,

    @SerializedName("statistics")
    val statistics: Statistics
)

data class Activity(
    @SerializedName("id")
    val id: Int,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("activity_type")
    val activityType: String,

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

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("formatted_date")
    val formattedDate: String
)

data class Statistics(
    @SerializedName("total_activities")
    val totalActivities: Int,

    @SerializedName("total_minutes")
    val totalMinutes: Int,

    @SerializedName("total_calories")
    val totalCalories: Int,

    @SerializedName("avg_duration")
    val avgDuration: Double
)