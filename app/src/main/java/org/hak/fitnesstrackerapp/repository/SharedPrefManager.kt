package org.hak.fitnesstrackerapp.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import org.hak.fitnesstrackerapp.network.models.AuthResponse

class SharedPrefManager private constructor(context: Context) {

    companion object {
        private const val PREF_NAME = "FitnessTrackerPref"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER = "user"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"

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

    fun saveAuthData(authResponse: AuthResponse) {
        authResponse.user?.let { user ->
            editor.putInt(KEY_USER_ID, user.id)
            editor.putString(KEY_USERNAME, user.username)
            editor.putString(KEY_EMAIL, user.email)
            editor.putString(KEY_USER, gson.toJson(user))
        }

        authResponse.token?.let { token ->
            editor.putString(KEY_TOKEN, token)
        }

        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun getUser(): org.hak.fitnesstrackerapp.network.models.UserResponse? {
        val userJson = sharedPreferences.getString(KEY_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, org.hak.fitnesstrackerapp.network.models.UserResponse::class.java)
        } else {
            null
        }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, -1)
    }

    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    fun getEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, null)
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clear() {
        editor.clear()
        editor.apply()
    }

    fun saveDailyReminderEnabled(enabled: Boolean) {
        editor.putBoolean("daily_reminder", enabled)
        editor.apply()
    }

    fun getDailyReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean("daily_reminder", false)
    }
}
