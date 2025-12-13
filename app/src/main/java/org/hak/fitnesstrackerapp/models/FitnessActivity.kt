package org.hak.fitnesstrackerapp.models

import java.util.Date

data class FitnessActivity(
    val id: Int = 0,
    val type: String = "",
    val duration: Int = 0, // in minutes
    val distance: Double = 0.0, // in kilometers
    val calories: Int = 0,
    val date: Date = Date(),
    val note: String = "",
    val location: String? = null,
    val steps: Int? = null
)