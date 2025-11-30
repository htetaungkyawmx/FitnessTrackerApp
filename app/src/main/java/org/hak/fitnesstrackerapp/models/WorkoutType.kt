package org.hak.fitnesstrackerapp.models

enum class WorkoutType {
    RUNNING,
    CYCLING,
    WEIGHTLIFTING;

    fun lowercase(): String {
        return this.name.lowercase()
    }
}
