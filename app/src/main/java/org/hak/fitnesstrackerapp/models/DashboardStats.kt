package org.hak.fitnesstrackerapp.models

data class DashboardStats(
    val steps: Int = 0,
    val calories: Int = 0,
    val distance: Double = 0.0,
    val duration: Int = 0,
    val totalActivities: Int = 0
)
