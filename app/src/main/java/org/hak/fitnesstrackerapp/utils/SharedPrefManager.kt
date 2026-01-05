package org.azm.fitness_app.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import org.azm.fitness_app.model.User

class SharedPrefManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "FitnessAppPrefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER = "user"

        @Volatile
        private var INSTANCE: SharedPrefManager? = null

        fun getInstance(context: Context): SharedPrefManager {
            return INSTANCE ?: synchronized(this) {
                SharedPrefManager(context).also { INSTANCE = it }
            }
        }
    }

    fun saveUser(user: User) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER, gson.toJson(user))
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    val user: User?
        get() {
            val userJson = sharedPreferences.getString(KEY_USER, null)
            return if (userJson != null) {
                gson.fromJson(userJson, User::class.java)
            } else {
                null
            }
        }

    val isLoggedIn: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)

    fun clear() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}