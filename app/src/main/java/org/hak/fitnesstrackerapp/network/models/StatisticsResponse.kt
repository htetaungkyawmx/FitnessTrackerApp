package org.hak.fitnesstrackerapp.network.models

data class StatisticsResponse(
    val totalActivities: Int,
    val totalMinutes: Int,
    val totalCalories: Int,
    val avgDuration: Double
)