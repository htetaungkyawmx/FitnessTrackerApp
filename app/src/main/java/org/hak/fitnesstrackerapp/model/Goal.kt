package org.hak.fitnesstrackerapp.model

import java.util.*

sealed class GoalType(val displayName: String, val unit: String) {
    object DailySteps : GoalType("Daily Steps", "steps")
    object WeeklyMinutes : GoalType("Weekly Exercise", "minutes")
    object MonthlyCalories : GoalType("Monthly Calories", "calories")
    object WeightLoss : GoalType("Weight Loss", "kg")
    object Distance : GoalType("Distance", "km")
    object Custom : GoalType("Custom", "")

    companion object {
        fun fromString(type: String): GoalType {
            return when (type.lowercase()) {
                "daily_steps", "steps" -> DailySteps
                "weekly_minutes", "minutes" -> WeeklyMinutes
                "monthly_calories", "calories" -> MonthlyCalories
                "weight_loss", "weight" -> WeightLoss
                "distance", "km" -> Distance
                else -> Custom
            }
        }
    }
}

data class Goal(
    val id: Int,
    val userId: Int,
    val type: GoalType,
    val targetValue: Double,
    val currentValue: Double,
    val deadline: Date? = null,
    val createdAt: Date,
    val updatedAt: Date? = null
) {
    val progress: Double
        get() = if (targetValue > 0) (currentValue / targetValue) * 100 else 0.0

    val isCompleted: Boolean
        get() = currentValue >= targetValue

    val daysRemaining: Int?
        get() = deadline?.let {
            val now = Calendar.getInstance()
            val deadlineCal = Calendar.getInstance().apply { time = it }
            val diff = deadlineCal.timeInMillis - now.timeInMillis
            (diff / (1000 * 60 * 60 * 24)).toInt()
        }

    fun updateProgress(newValue: Double): Goal {
        return copy(currentValue = newValue, updatedAt = Date())
    }

    fun addProgress(increment: Double): Goal {
        return copy(currentValue = currentValue + increment, updatedAt = Date())
    }
}