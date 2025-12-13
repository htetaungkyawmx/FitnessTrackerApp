package org.hak.fitnesstrackerapp.network.models

data class StatisticsResponse(
    val totalActivities: Int,
    val totalDuration: Int,
    val totalCalories: Int,
    val totalDistance: Double,
    val averageDuration: Double,
    val averageCalories: Double,
    val streak: Int,
    val activityDistribution: Map<String, Int>,
    val weeklyProgress: List<WeeklyProgress>,
    val monthlyComparison: MonthlyComparison
)

data class WeeklyProgress(
    val week: String,
    val activities: Int,
    val duration: Int,
    val calories: Int,
    val distance: Double
)

data class MonthlyComparison(
    val currentMonth: MonthStats,
    val previousMonth: MonthStats,
    val changePercentage: Double
)

data class MonthStats(
    val month: String,
    val activities: Int,
    val duration: Int,
    val calories: Int,
    val distance: Double
)

data class ChartDataResponse(
    val labels: List<String>,
    val datasets: List<ChartDataset>
)

data class ChartDataset(
    val label: String,
    val data: List<Double>,
    val color: String? = null
)