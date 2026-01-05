package org.azm.fitness_app.model

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val workoutId: Int? = null,
    val syncedIds: List<Any>? = null
)