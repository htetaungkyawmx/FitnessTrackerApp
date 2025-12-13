package org.hak.fitnesstrackerapp.model

import java.util.*

sealed class ActivityType(val displayName: String, val iconRes: String) {
    object Running : ActivityType("Running", "🏃")
    object Cycling : ActivityType("Cycling", "🚴")
    object Weightlifting : ActivityType("Weightlifting", "🏋️")
    object Swimming : ActivityType("Swimming", "🏊")
    object Yoga : ActivityType("Yoga", "🧘")
    object Walking : ActivityType("Walking", "🚶")
    object Other : ActivityType("Other", "🏅")

    companion object {
        fun fromString(type: String): ActivityType {
            return when (type.lowercase()) {
                "running" -> Running
                "cycling" -> Cycling
                "weightlifting" -> Weightlifting
                "swimming" -> Swimming
                "yoga" -> Yoga
                "walking" -> Walking
                else -> Other
            }
        }

        fun getAllTypes(): List<ActivityType> {
            return listOf(Running, Cycling, Weightlifting, Swimming, Yoga, Walking, Other)
        }
    }
}

data class Activity(
    val id: Int,
    val userId: Int,
    val type: ActivityType,
    val durationMinutes: Int,
    val distanceKm: Double? = null,
    val caloriesBurned: Int? = null,
    val notes: String? = null,
    val createdAt: Date,
    val location: Location? = null
) {
    data class Location(
        val latitude: Double,
        val longitude: Double,
        val altitude: Double? = null
    )

    companion object {
        fun calculateCalories(
            activityType: ActivityType,
            durationMinutes: Int,
            weightKg: Double? = null,
            distanceKm: Double? = null
        ): Int {
            // Simplified calorie calculation
            val metValue = when (activityType) {
                ActivityType.Running -> 9.8
                ActivityType.Cycling -> 7.5
                ActivityType.Weightlifting -> 6.0
                ActivityType.Swimming -> 8.0
                ActivityType.Yoga -> 2.5
                ActivityType.Walking -> 3.5
                ActivityType.Other -> 5.0
            }

            val weight = weightKg ?: 70.0 // Default weight
            return ((metValue * weight * durationMinutes) / 60).toInt()
        }

        fun calculatePace(distanceKm: Double, durationMinutes: Int): String {
            if (distanceKm <= 0) return "N/A"
            val paceMinPerKm = durationMinutes / distanceKm
            val minutes = paceMinPerKm.toInt()
            val seconds = ((paceMinPerKm - minutes) * 60).toInt()
            return String.format("%d:%02d min/km", minutes, seconds)
        }
    }

    val calories: Int
        get() = caloriesBurned ?: calculateCalories(type, durationMinutes)

    val pace: String?
        get() = distanceKm?.let { calculatePace(it, durationMinutes) }

    fun toDisplayString(): String {
        return "${type.displayName} - $durationMinutes min" +
                (distanceKm?.let { " - $it km" } ?: "") +
                (caloriesBurned?.let { " - $it cal" } ?: "")
    }
}