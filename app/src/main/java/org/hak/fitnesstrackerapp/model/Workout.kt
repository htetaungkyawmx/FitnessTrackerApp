package org.hak.fitnesstrackerapp.model

data class Workout(
    val id: Int = 0,
    val userId: Int = 0,
    val type: String, // "running", "cycling", "weightlifting"
    val duration: Int, // minutes
    val distance: Double? = 0.0, // km
    val calories: Int = 0,
    val notes: String = "",
    val date: String = "", // YYYY-MM-DD format
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false // For offline sync
)
