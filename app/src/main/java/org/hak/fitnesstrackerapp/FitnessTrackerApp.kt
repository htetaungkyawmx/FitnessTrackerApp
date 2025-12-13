package org.hak.fitnesstrackerapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import org.hak.fitnesstrackerapp.network.RetrofitClient
import org.hak.fitnesstrackerapp.repository.SharedPrefManager
import org.hak.fitnesstrackerapp.workers.DailyReminderWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class FitnessTrackerApp : Application() {

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "fitness_tracker_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Fitness Tracker Notifications"
        const val DAILY_REMINDER_WORK_NAME = "daily_reminder_work"

        lateinit var instance: FitnessTrackerApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Retrofit
        RetrofitClient.init()

        // Set auth token if user is logged in
        sharedPrefManager.getToken()?.let { token ->
            RetrofitClient.setAuthToken(token)
        }

        // Create notification channel
        createNotificationChannel()

        // Schedule daily reminder if enabled
        scheduleDailyReminder()
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

    private fun scheduleDailyReminder() {
        // Check if user has enabled daily reminders
        if (sharedPrefManager.getDailyReminderEnabled()) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequest.Builder(
                DailyReminderWorker::class.java,
                24, // Repeat interval
                TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                DAILY_REMINDER_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } else {
            // Cancel existing work
            WorkManager.getInstance(this).cancelUniqueWork(DAILY_REMINDER_WORK_NAME)
        }
    }

    fun updateDailyReminderSchedule(enabled: Boolean) {
        sharedPrefManager.setDailyReminderEnabled(enabled)
        scheduleDailyReminder()
    }
}
