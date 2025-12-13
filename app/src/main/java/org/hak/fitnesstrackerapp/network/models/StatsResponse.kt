package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName

data class StatsResponse(
    @SerializedName("weekly_stats")
    val weeklyStats: List<WeeklyStat>,

    @SerializedName("activity_types")
    val activityTypes: List<ActivityTypeStat>,

    @SerializedName("goals")
    val goals: List<GoalProgress>
)

data class WeeklyStat(
    @SerializedName("day")
    val day: String,

    @SerializedName("activity_count")
    val activityCount: Int,

    @SerializedName("total_minutes")
    val totalMinutes: Int,

    @SerializedName("total_calories")
    val totalCalories: Int
)

data class ActivityTypeStat(
    @SerializedName("activity_type")
    val activityType: String,

    @SerializedName("count")
    val count: Int,

    @SerializedName("total_minutes")
    val totalMinutes: Int
)

data class GoalProgress(
    @SerializedName("goal_type")
    val goalType: String,

    @SerializedName("target_value")
    val targetValue: Double,

    @SerializedName("current_value")
    val currentValue: Double,

    @SerializedName("progress_percentage")
    val progressPercentage: Double
)