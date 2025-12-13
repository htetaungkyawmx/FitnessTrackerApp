package org.hak.fitnesstrackerapp.model

import java.util.*

data class Statistics(
    val userId: Int,
    val period: Period,
    val activities: List<Activity>,
    val goals: List<Goal>
) {
    sealed class Period {
        object Daily : Period()
        object Weekly : Period()
        object Monthly : Period()
        object Yearly : Period()
        data class Custom(val startDate: Date, val endDate: Date) : Period()
    }

    val totalActivities: Int
        get() = activities.size

    val totalDuration: Int
        get() = activities.sumOf { it.durationMinutes }

    val totalCalories: Int
        get() = activities.sumOf { it.calories }

    val totalDistance: Double
        get() = activities.sumOf { it.distanceKm ?: 0.0 }

    val averageDuration: Double
        get() = if (activities.isNotEmpty()) totalDuration.toDouble() / activities.size else 0.0

    val mostCommonActivity: ActivityType?
        get() = activities.groupBy { it.type }
            .maxByOrNull { it.value.size }
            ?.key

    val activityDistribution: Map<ActivityType, Int>
        get() = activities.groupBy { it.type }
            .mapValues { it.value.size }

    val caloriesByDay: Map<String, Int>
        get() = activities.groupBy { it.createdAt.toFormattedDate() }
            .mapValues { entry -> entry.value.sumOf { it.calories } }

    fun calculateStreak(): Int {
        if (activities.isEmpty()) return 0

        val sortedDates = activities.map { it.createdAt }
            .sorted()
            .distinctBy { it.toFormattedDate() }

        var streak = 1
        var currentStreak = 1

        for (i in 1 until sortedDates.size) {
            val prevDate = sortedDates[i - 1]
            val currDate = sortedDates[i]

            if (isConsecutiveDays(prevDate, currDate)) {
                currentStreak++
                streak = maxOf(streak, currentStreak)
            } else {
                currentStreak = 1
            }
        }

        return streak
    }

    private fun isConsecutiveDays(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }

        cal1.add(Calendar.DAY_OF_YEAR, 1)
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun Date.toFormattedDate(): String {
        val cal = Calendar.getInstance().apply { time = this@toFormattedDate }
        return String.format("%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }
}