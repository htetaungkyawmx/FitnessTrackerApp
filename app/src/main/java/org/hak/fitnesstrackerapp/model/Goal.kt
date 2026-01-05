package org.hak.fitnesstrackerapp.model

data class Goal(
    val id: Int = 0,
    val userId: Int = 0,
    val type: String,
    val target: Double,
    val current: Double = 0.0,
    val deadline: String,
    val achieved: Boolean = false
)