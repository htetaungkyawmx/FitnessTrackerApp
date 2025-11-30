package org.hak.fitnesstrackerapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import org.hak.fitnesstrackerapp.models.User
import java.util.concurrent.TimeUnit

class PreferenceHelper(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("fitness_tracker_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // User Session Management
    fun saveUserSession(user: User) {
        val editor = sharedPreferences.edit()
        editor.putInt("user_id", user.id)
        editor.putString("username", user.username)
        editor.putString("email", user.email)
        editor.putString("user_data", gson.toJson(user))
        editor.putBoolean("is_logged_in", true)
        editor.putLong("login_time", System.currentTimeMillis())
        editor.apply()
    }

    fun getUserId(): Int {
        return sharedPreferences.getInt("user_id", -1)
    }

    fun getUsername(): String {
        return sharedPreferences.getString("username", "") ?: ""
    }

    fun getEmail(): String {
        return sharedPreferences.getString("email", "") ?: ""
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }

    fun isSessionValid(): Boolean {
        if (!isLoggedIn()) return false
        val loginTime = sharedPreferences.getLong("login_time", 0)
        val sessionDuration = System.currentTimeMillis() - loginTime
        return sessionDuration < TimeUnit.DAYS.toMillis(7)
    }

    fun getUser(): User? {
        val userJson = sharedPreferences.getString("user_data", null)
        return userJson?.let { gson.fromJson(it, User::class.java) }
    }

    fun clearSession() {
        val editor = sharedPreferences.edit()
        editor.remove("user_id")
        editor.remove("username")
        editor.remove("email")
        editor.remove("user_data")
        editor.remove("is_logged_in")
        editor.remove("login_time")
        editor.apply()
    }

    // App Settings
    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun isDarkMode(): Boolean {
        return sharedPreferences.getBoolean("dark_mode", false)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean("notifications_enabled", true)
    }

    // Statistics
    fun incrementWorkoutCount() {
        val count = getTotalWorkouts() + 1
        sharedPreferences.edit().putInt("total_workouts", count).apply()
    }

    fun getTotalWorkouts(): Int {
        return sharedPreferences.getInt("total_workouts", 0)
    }

    fun addCaloriesBurned(calories: Double) {
        val total = getTotalCaloriesBurned() + calories
        sharedPreferences.edit().putFloat("total_calories", total.toFloat()).apply()
    }

    fun getTotalCaloriesBurned(): Double {
        return sharedPreferences.getFloat("total_calories", 0f).toDouble()
    }

    fun clearAllData() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
