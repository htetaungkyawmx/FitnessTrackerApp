package org.hak.fitnesstrackerapp.models

data class User(
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val gender: String = "",
    val dailyGoal: Int = 10000
)