package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName

data class DailySummaryResponse(
    @SerializedName("date")
    val date: String,

    @SerializedName("summary")
    val summary: DailySummary,

    @SerializedName("activities")
    val activities: List<Activity>
)

data class DailySummary(
    @SerializedName("activity_count")
    val activityCount: Int,

    @SerializedName("total_minutes")
    val totalMinutes: Int,

    @SerializedName("total_calories")
    val totalCalories: Int,

    @SerializedName("activity_types")
    val activityTypes: String
)