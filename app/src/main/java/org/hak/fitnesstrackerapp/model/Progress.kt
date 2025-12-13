package org.hak.fitnesstrackerapp.model

data class Progress(
    val userId: Int,
    val date: String, // YYYY-MM-DD format
    val activities: Int = 0,
    val duration: Int = 0,
    val calories: Int = 0,
    val distance: Double = 0.0,
    val goalsAchieved: Int = 0
) {
    fun addActivity(activity: Activity): Progress {
        return copy(
            activities = activities + 1,
            duration = duration + activity.durationMinutes,
            calories = calories + activity.calories,
            distance = distance + (activity.distanceKm ?: 0.0)
        )
    }

    fun compareToPrevious(previous: Progress?): ProgressComparison {
        return if (previous == null) {
            ProgressComparison.NEW
        } else {
            when {
                activities > previous.activities && duration > previous.duration -> ProgressComparison.IMPROVED
                activities < previous.activities || duration < previous.duration -> ProgressComparison.DECLINED
                else -> ProgressComparison.SAME
            }
        }
    }
}

enum class ProgressComparison {
    NEW, IMPROVED, DECLINED, SAME
}