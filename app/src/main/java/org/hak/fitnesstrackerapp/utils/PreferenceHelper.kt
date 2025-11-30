package org.hak.fitnesstrackerapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.hak.fitnesstrackerapp.models.User
import java.util.*

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

    fun getUser(): User? {
        val userJson = sharedPreferences.getString("user_data", null)
        return userJson?.let { gson.fromJson(it, User::class.java) }
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

    fun setMetricSystem(metric: Boolean) {
        sharedPreferences.edit().putBoolean("metric_system", metric).apply()
    }

    fun isMetricSystem(): Boolean {
        return sharedPreferences.getBoolean("metric_system", true)
    }

    // Workout Preferences
    fun setDefaultWorkoutType(workoutType: String) {
        sharedPreferences.edit().putString("default_workout_type", workoutType).apply()
    }

    fun getDefaultWorkoutType(): String {
        return sharedPreferences.getString("default_workout_type", "RUNNING") ?: "RUNNING"
    }

    fun setAutoPauseEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("auto_pause", enabled).apply()
    }

    fun isAutoPauseEnabled(): Boolean {
        return sharedPreferences.getBoolean("auto_pause", true)
    }

    fun setVoiceFeedbackEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("voice_feedback", enabled).apply()
    }

    fun isVoiceFeedbackEnabled(): Boolean {
        return sharedPreferences.getBoolean("voice_feedback", false)
    }

    // Goal Preferences
    fun setWeeklyGoal(goalType: String, target: Double) {
        sharedPreferences.edit().putString("weekly_goal_type", goalType).apply()
        sharedPreferences.edit().putFloat("weekly_goal_target", target.toFloat()).apply()
    }

    fun getWeeklyGoal(): Pair<String, Double> {
        val type = sharedPreferences.getString("weekly_goal_type", "CALORIES") ?: "CALORIES"
        val target = sharedPreferences.getFloat("weekly_goal_target", 2000f).toDouble()
        return Pair(type, target)
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

    fun setLastSyncTime() {
        sharedPreferences.edit().putLong("last_sync_time", System.currentTimeMillis()).apply()
    }

    fun getLastSyncTime(): Long {
        return sharedPreferences.getLong("last_sync_time", 0)
    }

    // First Time Setup
    fun setFirstTimeSetupCompleted() {
        sharedPreferences.edit().putBoolean("first_time_setup", true).apply()
    }

    fun isFirstTimeSetup(): Boolean {
        return !sharedPreferences.getBoolean("first_time_setup", false)
    }

    // Workout Reminders
    fun setWorkoutReminderEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("workout_reminder", enabled).apply()
    }

    fun isWorkoutReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean("workout_reminder", true)
    }

    fun setReminderTime(hour: Int, minute: Int) {
        sharedPreferences.edit().putInt("reminder_hour", hour).apply()
        sharedPreferences.edit().putInt("reminder_minute", minute).apply()
    }

    fun getReminderTime(): Pair<Int, Int> {
        val hour = sharedPreferences.getInt("reminder_hour", 18) // 6 PM default
        val minute = sharedPreferences.getInt("reminder_minute", 0)
        return Pair(hour, minute)
    }

    // Achievement Tracking
    fun unlockAchievement(achievementId: String) {
        val achievements = getUnlockedAchievements().toMutableSet()
        achievements.add(achievementId)
        sharedPreferences.edit().putStringSet("unlocked_achievements", achievements).apply()
    }

    fun getUnlockedAchievements(): Set<String> {
        return sharedPreferences.getStringSet("unlocked_achievements", emptySet()) ?: emptySet()
    }

    fun hasAchievement(achievementId: String): Boolean {
        return getUnlockedAchievements().contains(achievementId)
    }

    // Clear all data (for logout)
    fun clearAllData() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
