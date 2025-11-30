package org.hak.fitnesstrackerapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("target_value")
    val targetValue: Double,

    @SerializedName("current_value")
    val currentValue: Double = 0.0,

    @SerializedName("unit")
    val unit: String,

    @SerializedName("deadline")
    val deadline: Date,

    @SerializedName("type")
    val type: GoalType,

    @SerializedName("is_completed")
    val isCompleted: Boolean = false,

    @SerializedName("created_at")
    val createdAt: Date = Date()
) {
    fun getProgressPercentage(): Double {
        return (currentValue / targetValue) * 100
    }

    fun getIconRes(): Int {
        return when (type) {
            GoalType.WEIGHT_LOSS -> R.drawable.ic_weight_loss
            GoalType.DISTANCE -> R.drawable.ic_distance
            GoalType.WORKOUT_COUNT -> R.drawable.ic_workout_count
            GoalType.CALORIES -> R.drawable.ic_calories
        }
    }
}

enum class GoalType {
    @SerializedName("WEIGHT_LOSS")
    WEIGHT_LOSS,

    @SerializedName("DISTANCE")
    DISTANCE,

    @SerializedName("WORKOUT_COUNT")
    WORKOUT_COUNT,

    @SerializedName("CALORIES")
    CALORIES
}
