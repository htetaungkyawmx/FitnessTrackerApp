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

    var userAge: Int
        get() = sharedPreferences.getInt("user_age", 0)
        set(value) = sharedPreferences.edit().putInt("user_age", value).apply()

    var userWeight: Float
        get() = sharedPreferences.getFloat("user_weight", 0f)
        set(value) = sharedPreferences.edit().putFloat("user_weight", value).apply()

    var userHeight: Float
        get() = sharedPreferences.getFloat("user_height", 0f)
        set(value) = sharedPreferences.edit().putFloat("user_height", value).apply()

    var userGender: String
        get() = sharedPreferences.getString("user_gender", "") ?: ""
        set(value) = sharedPreferences.edit().putString("user_gender", value).apply()

    var dailyGoal: Int
        get() = sharedPreferences.getInt("daily_goal", 10000)
        set(value) = sharedPreferences.edit().putInt("daily_goal", value).apply()

    fun saveUserData(userData: Map<String, Any>) {
        with(sharedPreferences.edit()) {
            putInt("user_id", (userData["id"] as? Number)?.toInt() ?: -1)
            putString("user_name", userData["name"] as? String ?: "")
            putString("user_email", userData["email"] as? String ?: "")
            putInt("user_age", (userData["age"] as? Number)?.toInt() ?: 0)
            putFloat("user_weight", (userData["weight"] as? Number)?.toFloat() ?: 0f)
            putFloat("user_height", (userData["height"] as? Number)?.toFloat() ?: 0f)
            putString("user_gender", userData["gender"] as? String ?: "")
            putInt("daily_goal", (userData["daily_goal"] as? Number)?.toInt() ?: 10000)
            apply()
        }
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}