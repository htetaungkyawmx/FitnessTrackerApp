package org.hak.fitnesstrackerapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import org.hak.fitnesstrackerapp.network.RetrofitClient
import org.hak.fitnesstrackerapp.repository.SharedPrefManager

class FitnessTrackerApp : Application() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "fitness_tracker_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Fitness Tracker Notifications"

        lateinit var instance: FitnessTrackerApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Retrofit
        RetrofitClient.init()

        // Set auth token if user is logged in
        val sharedPrefManager = SharedPrefManager.getInstance(this)
        sharedPrefManager.getToken()?.let { token ->
            RetrofitClient.setAuthToken(token)
        }

        // Create notification channel
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for fitness tracking reminders and updates"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}