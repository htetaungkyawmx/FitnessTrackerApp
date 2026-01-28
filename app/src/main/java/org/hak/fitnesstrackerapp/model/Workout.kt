package org.hak.fitnesstrackerapp.model

import java.io.Serializable

data class Workout(
    val id: Int = 0,
    val userId: Int = 0,
    val type: String,
    val duration: Int,
    val distance: Double? = 0.0,
    val calories: Int = 0,
    val notes: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
): Serializable
