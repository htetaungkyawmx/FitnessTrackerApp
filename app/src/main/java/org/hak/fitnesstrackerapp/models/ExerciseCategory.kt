package org.hak.fitnesstrackerapp.models

enum class ExerciseCategory {
    CHEST,
    BACK,
    LEGS,
    SHOULDERS,
    ARMS,
    CORE,
    CARDIO;

    fun getDisplayName(): String {
        return when (this) {
            CHEST -> "Chest"
            BACK -> "Back"
            LEGS -> "Legs"
            SHOULDERS -> "Shoulders"
            ARMS -> "Arms"
            CORE -> "Core"
            CARDIO -> "Cardio"
        }
    }

    companion object {
        fun fromString(value: String): ExerciseCategory {
            return when (value.uppercase()) {
                "CHEST" -> CHEST
                "BACK" -> BACK
                "LEGS" -> LEGS
                "SHOULDERS" -> SHOULDERS
                "ARMS" -> ARMS
                "CORE" -> CORE
                "CARDIO" -> CARDIO
                else -> CHEST // default
            }
        }
    }
}
