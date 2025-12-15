package org.hak.fitnesstrackerapp.models

data class Weightlifting(
    val id: Int = 0,
    val activityId: Int = 0,
    val exerciseName: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val weight: Double = 0.0
) {
    fun getTotalVolume(): Double {
        return sets * reps * weight
    }

    fun getOneRepMax(): Double {
        // Using Epley formula for 1RM calculation
        return weight * (1 + (0.0333 * reps))
    }
}