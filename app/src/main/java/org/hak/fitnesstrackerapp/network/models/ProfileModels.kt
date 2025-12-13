package org.hak.fitnesstrackerapp.network.models

data class UpdateProfileRequest(
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val birthDate: String? = null,
    val gender: String? = null,
    val fitnessLevel: String? = null
)

data class UpdateSettingsRequest(
    val notificationsEnabled: Boolean? = null,
    val darkMode: Boolean? = null,
    val measurementSystem: String? = null,
    val weeklyGoal: Int? = null,
    val dailyReminder: Boolean? = null
)

data class CompleteProfileResponse(
    val user: UserResponse,
    val statistics: UserStatisticsResponse,
    val recentActivities: List<ActivityResponse>,
    val activeGoals: List<GoalResponse>,
    val achievements: List<AchievementResponse>
)

data class UserStatisticsResponse(
    val memberSince: String,
    val totalActivities: Int,
    val totalDuration: Int,
    val totalCalories: Int,
    val totalDistance: Double,
    val currentStreak: Int,
    val longestStreak: Int,
    val goalsCompleted: Int,
    val achievementsUnlocked: Int
)

data class AchievementResponse(
    val id: Int,
    val name: String,
    val description: String,
    val icon: String,
    val unlockedAt: String?,
    val progress: Double?,
    val isUnlocked: Boolean
)