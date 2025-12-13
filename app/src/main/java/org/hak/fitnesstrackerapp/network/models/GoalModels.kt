package org.hak.fitnesstrackerapp.network.models

data class GoalRequest(
    val type: String,
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val deadline: String? = null,
    val unit: String? = null
)

data class GoalResponse(
    val id: Int,
    val userId: Int,
    val type: String,
    val displayName: String,
    val targetValue: Double,
    val currentValue: Double,
    val progress: Double,
    val unit: String,
    val deadline: String?,
    val isCompleted: Boolean,
    val daysRemaining: Int?,
    val createdAt: String,
    val updatedAt: String?
)

data class GoalsResponse(
    val goals: List<GoalResponse>,
    val progress: Map<String, Double>
)

data class UpdateGoalProgressRequest(
    val goalId: Int,
    val increment: Double
)