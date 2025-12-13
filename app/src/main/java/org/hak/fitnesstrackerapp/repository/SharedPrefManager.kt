package org.hak.fitnesstrackerapp.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import org.hak.fitnesstrackerapp.network.models.AuthResponse
import org.hak.fitnesstrackerapp.network.models.UserResponse

class SharedPrefManager private constructor(context: Context) {

    companion object {
        private const val PREF_NAME = "FitnessTrackerPref"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER = "user"
        private const val KEY_TOKEN = "token"
        private const val KEY_DAILY_REMINDER = "daily_reminder"

        @Volatile
        private var INSTANCE: SharedPrefManager? = null

        fun getInstance(context: Context): SharedPrefManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SharedPrefManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val gson = Gson()

    // User methods
    fun saveUser(user: AuthResponse) {
        editor.putString(KEY_USER, gson.toJson(user))
        editor.putString(KEY_TOKEN, user.token)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun getUser(): UserResponse? {
        val userJson = sharedPreferences.getString(KEY_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, UserResponse::class.java)
        } else {
            null
        }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Daily reminder methods
    fun getDailyReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_DAILY_REMINDER, true) // Default to true
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        editor.putBoolean(KEY_DAILY_REMINDER, enabled)
        editor.apply()
    }

    // Clear all data
    fun clear() {
        editor.clear()
        editor.apply()
    }
}