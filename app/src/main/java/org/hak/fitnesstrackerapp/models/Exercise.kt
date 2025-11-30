package org.hak.fitnesstrackerapp.models

import android.os.Parcelable
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.hak.fitnesstrackerapp.database.Converters

@Parcelize
@TypeConverters(Converters::class)
data class Exercise(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name")
    val name: String,

    @SerializedName("sets")
    val sets: Int,

    @SerializedName("reps")
    val reps: Int,

    @SerializedName("weight")
    val weight: Double, // in kg

    @SerializedName("rest_time")
    val restTime: Int = 60, // in seconds

    @SerializedName("completed_sets")
    val completedSets: Int = 0,

    @SerializedName("notes")
    val notes: String = "",

    @SerializedName("category")
    val category: ExerciseCategory = ExerciseCategory.STRENGTH,

    @SerializedName("created_at")
    val createdAt: java.util.Date = java.util.Date(),

    @SerializedName("updated_at")
    val updatedAt: java.util.Date = java.util.Date()
) : Parcelable {

    fun getTotalVolume(): Double {
        return sets * reps * weight
    }

    fun getProgressPercentage(): Double {
        return if (sets > 0) (completedSets.toDouble() / sets) * 100 else 0.0
    }

    fun isCompleted(): Boolean {
        return completedSets >= sets
    }

    fun getFormattedDetails(): String {
        return "$sets x $reps @ ${weight}kg"
    }

    fun getOneRepMax(): Double {
        // Calculate estimated one-rep max using Epley formula
        return weight * (1 + (reps / 30.0))
    }

    companion object {
        fun createDefaultExercises(): List<Exercise> {
            return listOf(
                Exercise(
                    name = "Bench Press",
                    sets = 3,
                    reps = 10,
                    weight = 60.0,
                    category = ExerciseCategory.CHEST
                ),
                Exercise(
                    name = "Squats",
                    sets = 4,
                    reps = 8,
                    weight = 80.0,
                    category = ExerciseCategory.LEGS
                ),
                Exercise(
                    name = "Deadlift",
                    sets = 3,
                    reps = 6,
                    weight = 100.0,
                    category = ExerciseCategory.BACK
                ),
                Exercise(
                    name = "Shoulder Press",
                    sets = 3,
                    reps = 12,
                    weight = 30.0,
                    category = ExerciseCategory.SHOULDERS
                ),
                Exercise(
                    name = "Bicep Curls",
                    sets = 3,
                    reps = 15,
                    weight = 15.0,
                    category = ExerciseCategory.ARMS
                )
            )
        }
    }
}

enum class ExerciseCategory {
    @SerializedName("CHEST")
    CHEST,

    @SerializedName("BACK")
    BACK,

    @SerializedName("LEGS")
    LEGS,

    @SerializedName("SHOULDERS")
    SHOULDERS,

    @SerializedName("ARMS")
    ARMS,

    @SerializedName("CORE")
    CORE,

    @SerializedName("CARDIO")
    CARDIO,

    @SerializedName("STRENGTH")
    STRENGTH,

    @SerializedName("FLEXIBILITY")
    FLEXIBILITY,

    @SerializedName("OTHER")
    OTHER;

    fun getDisplayName(): String {
        return when (this) {
            CHEST -> "Chest"
            BACK -> "Back"
            LEGS -> "Legs"
            SHOULDERS -> "Shoulders"
            ARMS -> "Arms"
            CORE -> "Core"
            CARDIO -> "Cardio"
            STRENGTH -> "Strength"
            FLEXIBILITY -> "Flexibility"
            OTHER -> "Other"
        }
    }

    fun getIconRes(): Int {
        return when (this) {
            CHEST -> org.hak.fitnesstrackerapp.R.drawable.ic_chest
            BACK -> org.hak.fitnesstrackerapp.R.drawable.ic_back
            LEGS -> org.hak.fitnesstrackerapp.R.drawable.ic_legs
            SHOULDERS -> org.hak.fitnesstrackerapp.R.drawable.ic_shoulders
            ARMS -> org.hak.fitnesstrackerapp.R.drawable.ic_arms
            CORE -> org.hak.fitnesstrackerapp.R.drawable.ic_core
            CARDIO -> org.hak.fitnesstrackerapp.R.drawable.ic_cardio
            STRENGTH -> org.hak.fitnesstrackerapp.R.drawable.ic_strength
            FLEXIBILITY -> org.hak.fitnesstrackerapp.R.drawable.ic_flexibility
            OTHER -> org.hak.fitnesstrackerapp.R.drawable.ic_exercise
        }
    }
}

data class ExerciseSet(
    @SerializedName("set_number")
    val setNumber: Int,

    @SerializedName("reps_completed")
    val repsCompleted: Int,

    @SerializedName("weight_used")
    val weightUsed: Double,

    @SerializedName("is_completed")
    val isCompleted: Boolean = false,

    @SerializedName("rest_timer")
    val restTimer: Int = 0
) {
    fun getSetSummary(): String {
        return "Set $setNumber: $repsCompleted reps @ ${weightUsed}kg"
    }
}

data class ExerciseSession(
    @SerializedName("exercise_id")
    val exerciseId: Int,

    @SerializedName("sets_completed")
    val setsCompleted: List<ExerciseSet>,

    @SerializedName("start_time")
    val startTime: java.util.Date,

    @SerializedName("end_time")
    val endTime: java.util.Date? = null,

    @SerializedName("total_volume")
    val totalVolume: Double = 0.0
) {
    fun getSessionDuration(): Long {
        return endTime?.time?.minus(startTime.time) ?: 0
    }

    fun calculateTotalVolume(): Double {
        return setsCompleted.sumOf { it.repsCompleted * it.weightUsed }
    }
}
