package org.hak.fitnesstrackerapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.hak.fitnesstrackerapp.R

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val description: String,
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val unit: String,
    val deadline: Long,
    val type: GoalType,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getProgressPercentage(): Double {
        return if (targetValue > 0) (currentValue / targetValue) * 100 else 0.0
    }

    fun getIconRes(): Int {
        return when (type) {
            GoalType.WEIGHT_LOSS -> R.drawable.ic_weight
            GoalType.DISTANCE -> R.drawable.ic_distance
            GoalType.WORKOUT_COUNT -> R.drawable.ic_workout
            GoalType.CALORIES -> R.drawable.ic_calories
        }
    }
}

enum class GoalType {
    WEIGHT_LOSS, DISTANCE, WORKOUT_COUNT, CALORIES
}
