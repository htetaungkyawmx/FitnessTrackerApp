package org.hak.fitnesstrackerapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: ExerciseCategory,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val completedSets: Int = 0,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
) {
    fun getTotalVolume(): Double = sets * reps * weight
    fun getProgressPercentage(): Double = if (sets > 0) (completedSets.toDouble() / sets) * 100 else 0.0

    // FIXED: Renamed the function to avoid conflict with property
    fun checkCompletion(): Boolean = completedSets >= sets

    fun getOneRepMax(): Double = weight * (1 + reps / 30.0)
    fun getFormattedDetails(): String = "$sets x $reps ${if (weight > 0) "@ ${weight}kg" else "bodyweight"}"
}
