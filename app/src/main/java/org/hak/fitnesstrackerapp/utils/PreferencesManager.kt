package org.hak.fitnesstrackerapp.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("FitnessTrackerPrefs", Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = sharedPreferences.getBoolean("is_logged_in", false)
        set(value) = sharedPreferences.edit().putBoolean("is_logged_in", value).apply()

    var userId: Int
        get() = sharedPreferences.getInt("user_id", -1)
        set(value) = sharedPreferences.edit().putInt("user_id", value).apply()

    var userEmail: String
        get() = sharedPreferences.getString("user_email", "") ?: ""
        set(value) = sharedPreferences.edit().putString("user_email", value).apply()

    var userName: String
        get() = sharedPreferences.getString("user_name", "") ?: ""
        set(value) = sharedPreferences.edit().putString("user_name", value).apply()

    var dailyGoal: Int
        get() = sharedPreferences.getInt("daily_goal", 10000)
        set(value) = sharedPreferences.edit().putInt("daily_goal", value).apply()

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}