package org.hak.fitnesstrackerapp.utils

import org.hak.fitnesstrackerapp.models.Workout
import org.hak.fitnesstrackerapp.models.WorkoutType
import kotlin.math.max

object WorkoutCalculator {

    fun calculateCaloriesBurned(
        workoutType: WorkoutType,
        duration: Int,
        weight: Double? = null,
        distance: Double? = null,
        averageSpeed: Double? = null
    ): Double {
        val baseCalories = when (workoutType) {
            WorkoutType.RUNNING -> calculateRunningCalories(duration, weight, distance, averageSpeed)
            WorkoutType.CYCLING -> calculateCyclingCalories(duration, weight, distance, averageSpeed)
            WorkoutType.WEIGHTLIFTING -> calculateWeightliftingCalories(duration, weight)
        }
        return max(baseCalories, 0.0)
    }

    private fun calculateRunningCalories(
        duration: Int,
        weight: Double?,
        distance: Double?,
        averageSpeed: Double?
    ): Double {
        val userWeight = weight ?: 70.0 // Default weight 70kg
        val caloriesPerMinute = when {
            averageSpeed != null && averageSpeed > 12 -> 0.2 * userWeight // Fast running
            averageSpeed != null && averageSpeed > 8 -> 0.15 * userWeight // Jogging
            else -> 0.1 * userWeight // Walking
        }
        return caloriesPerMinute * duration
    }

    private fun calculateCyclingCalories(
        duration: Int,
        weight: Double?,
        distance: Double?,
        averageSpeed: Double?
    ): Double {
        val userWeight = weight ?: 70.0
        val caloriesPerMinute = when {
            averageSpeed != null && averageSpeed > 25 -> 0.18 * userWeight // Fast cycling
            averageSpeed != null && averageSpeed > 15 -> 0.12 * userWeight // Moderate cycling
            else -> 0.08 * userWeight // Leisurely cycling
        }
        return caloriesPerMinute * duration
    }

    private fun calculateWeightliftingCalories(duration: Int, weight: Double?): Double {
        val userWeight = weight ?: 70.0
        // Weightlifting burns fewer calories but includes afterburn effect
        val baseCalories = 0.05 * userWeight * duration
        val afterburnEffect = baseCalories * 0.15 // 15% afterburn effect
        return baseCalories + afterburnEffect
    }

    fun calculatePace(duration: Int, distance: Double): Double {
        return if (distance > 0) duration.toDouble() / distance else 0.0
    }

    fun calculateAverageSpeed(distance: Double, duration: Int): Double {
        return if (duration > 0) (distance / duration.toDouble()) * 60 else 0.0
    }

    fun calculateWorkoutIntensity(workout: Workout): String {
        val caloriesPerMinute = workout.calories.toDouble() / workout.duration
        return when {
            caloriesPerMinute > 12 -> "Very High"
            caloriesPerMinute > 8 -> "High"
            caloriesPerMinute > 5 -> "Moderate"
            caloriesPerMinute > 3 -> "Low"
            else -> "Very Low"
        }
    }

    fun calculateWeeklyProgress(workouts: List<Workout>): Map<String, Double> {
        val currentWeekCalories = workouts.sumOf { it.calories.toDouble() } // Convert to Double
        val currentWeekDuration = workouts.sumOf { it.duration.toDouble() } // Convert to Double
        val currentWeekWorkouts = workouts.size.toDouble() // Convert to Double

        // For demo purposes, using previous week as 80% of current
        val previousWeekCalories = currentWeekCalories * 0.8
        val previousWeekDuration = currentWeekDuration * 0.8
        val previousWeekWorkouts = currentWeekWorkouts * 0.8

        return mapOf(
            "calories_progress" to calculateProgressPercentage(currentWeekCalories, previousWeekCalories),
            "duration_progress" to calculateProgressPercentage(currentWeekDuration, previousWeekDuration),
            "workouts_progress" to calculateProgressPercentage(currentWeekWorkouts, previousWeekWorkouts)
        )
    }

    private fun calculateProgressPercentage(current: Double, previous: Double): Double {
        return if (previous > 0) ((current - previous) / previous) * 100 else 100.0
    }

    fun estimateRecoveryTime(workout: Workout): Int {
        val baseRecovery = when (workout.type) {
            WorkoutType.RUNNING -> workout.duration * 0.5
            WorkoutType.CYCLING -> workout.duration * 0.4
            WorkoutType.WEIGHTLIFTING -> workout.duration * 0.6
        }
        val intensityFactor = when (calculateWorkoutIntensity(workout)) {
            "Very High" -> 1.5
            "High" -> 1.2
            "Moderate" -> 1.0
            "Low" -> 0.8
            else -> 0.6
        }
        return (baseRecovery * intensityFactor).toInt()
    }

    fun calculateFitnessScore(workouts: List<Workout>): Double {
        if (workouts.isEmpty()) return 0.0

        val totalCalories = workouts.sumOf { it.calories.toDouble() } // Convert to Double
        val totalDuration = workouts.sumOf { it.duration.toDouble() } // Convert to Double
        val workoutFrequency = workouts.size.toDouble() // Convert to Double
        val averageIntensity = workouts.sumOf {
            when (calculateWorkoutIntensity(it)) {
                "Very High" -> 5.0
                "High" -> 4.0
                "Moderate" -> 3.0
                "Low" -> 2.0
                else -> 1.0
            }
        } / workouts.size

        return (totalCalories * 0.3 + totalDuration * 0.2 + workoutFrequency * 20 + averageIntensity * 10)
    }
}
