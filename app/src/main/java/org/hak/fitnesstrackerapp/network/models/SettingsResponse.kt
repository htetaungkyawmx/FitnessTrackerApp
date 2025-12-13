package org.hak.fitnesstrackerapp.network.models

import com.google.gson.annotations.SerializedName

data class SettingsResponse(
    @SerializedName("notifications_enabled")
    val notificationsEnabled: Boolean,

    @SerializedName("dark_mode")
    val darkMode: Boolean,

    @SerializedName("measurement_system")
    val measurementSystem: String, // "metric" or "imperial"

    @SerializedName("weekly_goal")
    val weeklyGoal: Int,

    @SerializedName("daily_reminder")
    val dailyReminder: Boolean
)