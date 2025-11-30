package org.hak.fitnesstrackerapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String,
    val sets: Int = 3,
    val reps: Int = 10,
    val weight: Double = 0.0,
    val completedSets: Int = 0,
    val notes: String = ""
) {
    fun getFormattedDetails(): String {
        return "$sets x $reps ${if (weight > 0) "@ ${weight}kg" else ""}"
    }

    fun getProgressPercentage(): Double {
        return if (sets > 0) (completedSets.toDouble() / sets) * 100 else 0.0
    }

    fun getTotalVolume(): Double {
        return sets * reps * weight
    }

    fun getOneRepMax(): Double {
        // Simple 1RM calculation using Epley formula
        return weight * (1 + reps / 30.0)
    }

    fun isCompleted(): Boolean {
        return completedSets >= sets
    }
}

// Exercise category constants
object ExerciseCategory {
    const val CHEST = "Chest"
    const val BACK = "Back"
    const val LEGS = "Legs"
    const val SHOULDERS = "Shoulders"
    const val ARMS = "Arms"
    const val CORE = "Core"
    const val CARDIO = "Cardio"
    const val STRENGTH = "Strength"
    const val FLEXIBILITY = "Flexibility"
    const val OTHER = "Other"
}

// Extension functions for Exercise category
fun String.getDisplayName(): String {
    return this
}

fun String.getIconRes(): Int {
    return when (this.lowercase()) {
        "chest" -> org.hak.fitnesstrackerapp.R.drawable.ic_chest
        "back" -> org.hak.fitnesstrackerapp.R.drawable.ic_back
        "legs" -> org.hak.fitnesstrackerapp.R.drawable.ic_legs
        "shoulders" -> org.hak.fitnesstrackerapp.R.drawable.ic_shoulders
        "arms" -> org.hak.fitnesstrackerapp.R.drawable.ic_arms
        "core" -> org.hak.fitnesstrackerapp.R.drawable.ic_core
        "cardio" -> org.hak.fitnesstrackerapp.R.drawable.ic_cardio
        "strength" -> org.hak.fitnesstrackerapp.R.drawable.ic_strength
        "flexibility" -> org.hak.fitnesstrackerapp.R.drawable.ic_flexibility
        else -> org.hak.fitnesstrackerapp.R.drawable.ic_placeholder
    }
}
