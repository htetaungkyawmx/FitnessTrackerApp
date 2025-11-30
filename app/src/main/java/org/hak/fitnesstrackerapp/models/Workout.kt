package org.hak.fitnesstrackerapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import org.hak.fitnesstrackerapp.database.Converters
import java.util.Date

@Entity(tableName = "workouts")
@TypeConverters(Converters::class)
data class Workout(
    @PrimaryKey
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("type")
    val type: WorkoutType,

    @SerializedName("duration")
    val duration: Int, // in minutes

    @SerializedName("calories")
    val calories: Double,

    @SerializedName("date")
    val date: Date,

    @SerializedName("notes")
    val notes: String = "",

    @SerializedName("distance")
    val distance: Double? = null, // for running/cycling

    @SerializedName("average_speed")
    val averageSpeed: Double? = null, // for running/cycling

    @SerializedName("elevation")
    val elevation: Double? = null, // for cycling

    @SerializedName("exercises")
    val exercises: List<Exercise> = emptyList() // for weightlifting
) {
    fun getWorkoutSummary(): String {
        return when (type) {
            WorkoutType.RUNNING -> "Ran ${distance}km in $duration minutes"
            WorkoutType.CYCLING -> "Cycled ${distance}km in $duration minutes"
            WorkoutType.WEIGHTLIFTING -> "Weightlifting: ${exercises.size} exercises"
        }
    }

    fun getIconRes(): Int {
        return when (type) {
            WorkoutType.RUNNING -> R.drawable.ic_running
            WorkoutType.CYCLING -> R.drawable.ic_cycling
            WorkoutType.WEIGHTLIFTING -> R.drawable.ic_weightlifting
        }
    }
}

enum class WorkoutType {
    @SerializedName("RUNNING")
    RUNNING,

    @SerializedName("CYCLING")
    CYCLING,

    @SerializedName("WEIGHTLIFTING")
    WEIGHTLIFTING
}

data class Exercise(
    @SerializedName("name")
    val name: String,

    @SerializedName("sets")
    val sets: Int,

    @SerializedName("reps")
    val reps: Int,

    @SerializedName("weight")
    val weight: Double // in kg
)
