package org.hak.fitnesstrackerapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.ui.activities.MainActivity

class NotificationService(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Workout reminder channel
            val workoutChannel = NotificationChannel(
                WORKOUT_CHANNEL_ID,
                "Workout Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for workouts and fitness goals"
            }

            // Achievement channel
            val achievementChannel = NotificationChannel(
                ACHIEVEMENT_CHANNEL_ID,
                "Achievements",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for fitness achievements"
            }

            notificationManager.createNotificationChannel(workoutChannel)
            notificationManager.createNotificationChannel(achievementChannel)
        }
    }

    fun showWorkoutReminder() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, WORKOUT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_fitness)
            .setContentTitle("Time for your workout! 💪")
            .setContentText("Don't forget to log your daily exercise")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(WORKOUT_REMINDER_ID, notification)
    }

    fun showGoalAchieved(goalTitle: String) {
        val notification = NotificationCompat.Builder(context, ACHIEVEMENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_achievement)
            .setContentTitle("Goal Achieved! 🎉")
            .setContentText("You've completed: $goalTitle")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(GOAL_ACHIEVED_ID, notification)
    }

    fun showWorkoutCompleted(workoutType: String, calories: Double) {
        val notification = NotificationCompat.Builder(context, ACHIEVEMENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_workout)
            .setContentTitle("Workout Completed! ✅")
            .setContentText("Great job! You burned ${String.format("%.0f", calories)} calories with $workoutType")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(WORKOUT_COMPLETED_ID, notification)
    }

    companion object {
        private const val WORKOUT_CHANNEL_ID = "workout_reminders"
        private const val ACHIEVEMENT_CHANNEL_ID = "achievements"
        private const val WORKOUT_REMINDER_ID = 1
        private const val GOAL_ACHIEVED_ID = 2
        private const val WORKOUT_COMPLETED_ID = 3
    }
}
