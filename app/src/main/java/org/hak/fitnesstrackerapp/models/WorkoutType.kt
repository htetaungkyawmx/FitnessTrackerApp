package org.hak.fitnesstrackerapp.models

import org.hak.fitnesstrackerapp.R

enum class WorkoutType {
    RUNNING, CYCLING, WEIGHTLIFTING;

    override fun toString(): String {
        return when (this) {
            RUNNING -> "Running"
            CYCLING -> "Cycling"
            WEIGHTLIFTING -> "Weightlifting"
        }
    }

    companion object {
        fun fromString(value: String): WorkoutType {
            return when (value.uppercase()) {
                "RUNNING" -> RUNNING
                "CYCLING" -> CYCLING
                "WEIGHTLIFTING" -> WEIGHTLIFTING
                else -> RUNNING
            }
        }
    }
}

// Extension function for WorkoutType
fun WorkoutType.getIconRes(): Int {
    return when (this) {
        WorkoutType.RUNNING -> R.drawable.ic_running
        WorkoutType.CYCLING -> R.drawable.ic_cycling
        WorkoutType.WEIGHTLIFTING -> R.drawable.ic_weightlifting
    }
}
