package org.hak.fitnesstrackerapp.models

import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.utils.DateUtils
import java.util.concurrent.TimeUnit

// Extension functions for Workout
fun Workout.getWorkoutSummary(): String {
    return "${this.type.name} - ${this.duration} min - ${this.calories.toInt()} cal"
}

fun Workout.formatWorkoutDate(): String {
    return DateUtils.formatDate(this.date.time)
}

fun Workout.getIconRes(): Int {
    return when (this.type) {
        WorkoutType.RUNNING -> R.drawable.ic_running
        WorkoutType.CYCLING -> R.drawable.ic_cycling
        WorkoutType.WEIGHTLIFTING -> R.drawable.ic_weightlifting
    }
}

// Extension functions for Long (duration)
fun Long.formatDuration(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
