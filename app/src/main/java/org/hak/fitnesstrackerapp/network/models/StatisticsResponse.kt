package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName

data class StatisticsResponse(
    @SerializedName("total_activities") val totalActivities: Int,
    @SerializedName("total_minutes") val totalMinutes: Int,
    @SerializedName("total_calories") val totalCalories: Int,
    @SerializedName("total_distance") val totalDistance: Double? = null,
    @SerializedName("average_duration") val averageDuration: Double? = null
)

// Simple Statistics class for local use
data class SimpleStatistics(
    val totalActivities: Int,
    val totalMinutes: Int,
    val totalCalories: Int,
    val totalDistance: Double = 0.0
)