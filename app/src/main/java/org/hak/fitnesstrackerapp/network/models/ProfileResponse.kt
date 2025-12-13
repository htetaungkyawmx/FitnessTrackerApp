package org.hak.fitnesstrackerapp.network.models

import com.google.android.gms.fitness.data.Goal
import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("profile")
    val profile: UserProfile,

    @SerializedName("statistics")
    val statistics: UserStatistics,

    @SerializedName("goals")
    val goals: List<Goal>
)

data class UserProfile(
    @SerializedName("id")
    val id: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("height_cm")
    val heightCm: Double?,

    @SerializedName("weight_kg")
    val weightKg: Double?,

    @SerializedName("birth_date")
    val birthDate: String?,

    @SerializedName("created_at")
    val createdAt: String
)

data class UserStatistics(
    @SerializedName("total_activities")
    val totalActivities: Int,

    @SerializedName("total_minutes")
    val totalMinutes: Int,

    @SerializedName("total_calories")
    val totalCalories: Int,

    @SerializedName("last_activity")
    val lastActivity: String?
)

data class UserProfileResponse(
    @SerializedName("user")
    val user: UserProfile
)