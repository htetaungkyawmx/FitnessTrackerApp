package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName

// Common Response Models
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)

data class ActivityIdResponse(
    @SerializedName("activity_id") val activityId: Int
)

data class DeleteActivityRequest(
    @SerializedName("activity_id") val activityId: Int
)

data class UpdateProfileRequest(
    @SerializedName("height_cm") val heightCm: Double? = null,
    @SerializedName("weight_kg") val weightKg: Double? = null,
    @SerializedName("birth_date") val birthDate: String? = null
)

data class UserProfileResponse(
    @SerializedName("user") val user: UserResponse
)

data class ProfileResponse(
    @SerializedName("profile") val profile: UserResponse,
    @SerializedName("statistics") val statistics: UserStatistics,
    @SerializedName("goals") val goals: List<GoalResponse>
)

data class UserStatistics(
    @SerializedName("total_activities") val totalActivities: Int,
    @SerializedName("total_minutes") val totalMinutes: Int,
    @SerializedName("total_calories") val totalCalories: Int,
    @SerializedName("last_activity") val lastActivity: String?
)

data class GoalRequest(
    @SerializedName("goal_type") val goalType: String,
    @SerializedName("target_value") val targetValue: Double,
    @SerializedName("current_value") val currentValue: Double = 0.0,
    @SerializedName("deadline") val deadline: String? = null
)

data class GoalResponse(
    @SerializedName("goal_type") val goalType: String,
    @SerializedName("target_value") val targetValue: Double,
    @SerializedName("current_value") val currentValue: Double,
    @SerializedName("progress") val progress: Double
)

data class ChangePasswordRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("confirm_password") val confirmPassword: String
)

// Paginated Response
data class PaginatedActivitiesResponse(
    @SerializedName("activities") val activities: List<ActivityResponse>,
    @SerializedName("pagination") val pagination: Pagination
)

data class Pagination(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_items") val totalItems: Int,
    @SerializedName("items_per_page") val itemsPerPage: Int
)

// Daily Summary
data class DailySummaryResponse(
    @SerializedName("date") val date: String,
    @SerializedName("summary") val summary: DailySummary,
    @SerializedName("activities") val activities: List<ActivityResponse>
)

data class DailySummary(
    @SerializedName("activity_count") val activityCount: Int,
    @SerializedName("total_minutes") val totalMinutes: Int,
    @SerializedName("total_calories") val totalCalories: Int,
    @SerializedName("activity_types") val activityTypes: String
)

// Stats
data class StatsResponse(
    @SerializedName("weekly_stats") val weeklyStats: List<WeeklyStat>,
    @SerializedName("activity_types") val activityTypes: List<ActivityTypeStat>,
    @SerializedName("goals") val goals: List<GoalProgress>
)

data class WeeklyStat(
    @SerializedName("day") val day: String,
    @SerializedName("activity_count") val activityCount: Int,
    @SerializedName("total_minutes") val totalMinutes: Int,
    @SerializedName("total_calories") val totalCalories: Int
)

data class ActivityTypeStat(
    @SerializedName("activity_type") val activityType: String,
    @SerializedName("count") val count: Int,
    @SerializedName("total_minutes") val totalMinutes: Int
)

data class GoalProgress(
    @SerializedName("goal_type") val goalType: String,
    @SerializedName("target_value") val targetValue: Double,
    @SerializedName("current_value") val currentValue: Double,
    @SerializedName("progress_percentage") val progressPercentage: Double
)
