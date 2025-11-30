package org.hak.fitnesstrackerapp.managers

import org.hak.fitnesstrackerapp.models.Exercise
import org.hak.fitnesstrackerapp.models.ExerciseCategory

object ExerciseManager {

    private val defaultExercises = listOf(
        // Chest Exercises
        Exercise(name = "Bench Press", category = ExerciseCategory.CHEST, sets = 3, reps = 10, weight = 60.0),
        Exercise(name = "Incline Bench Press", category = ExerciseCategory.CHEST, sets = 3, reps = 10, weight = 50.0),
        Exercise(name = "Dumbbell Flyes", category = ExerciseCategory.CHEST, sets = 3, reps = 12, weight = 15.0),
        Exercise(name = "Push Ups", category = ExerciseCategory.CHEST, sets = 3, reps = 15, weight = 0.0),

        // Back Exercises
        Exercise(name = "Deadlift", category = ExerciseCategory.BACK, sets = 3, reps = 6, weight = 100.0),
        Exercise(name = "Pull Ups", category = ExerciseCategory.BACK, sets = 3, reps = 8, weight = 0.0),
        Exercise(name = "Bent Over Rows", category = ExerciseCategory.BACK, sets = 3, reps = 10, weight = 40.0),
        Exercise(name = "Lat Pulldowns", category = ExerciseCategory.BACK, sets = 3, reps = 12, weight = 45.0),

        // Leg Exercises
        Exercise(name = "Squats", category = ExerciseCategory.LEGS, sets = 4, reps = 8, weight = 80.0),
        Exercise(name = "Lunges", category = ExerciseCategory.LEGS, sets = 3, reps = 12, weight = 20.0),
        Exercise(name = "Leg Press", category = ExerciseCategory.LEGS, sets = 3, reps = 10, weight = 100.0),
        Exercise(name = "Calf Raises", category = ExerciseCategory.LEGS, sets = 3, reps = 15, weight = 50.0),

        // Shoulder Exercises
        Exercise(name = "Shoulder Press", category = ExerciseCategory.SHOULDERS, sets = 3, reps = 10, weight = 30.0),
        Exercise(name = "Lateral Raises", category = ExerciseCategory.SHOULDERS, sets = 3, reps = 12, weight = 10.0),
        Exercise(name = "Front Raises", category = ExerciseCategory.SHOULDERS, sets = 3, reps = 12, weight = 10.0),
        Exercise(name = "Shrugs", category = ExerciseCategory.SHOULDERS, sets = 3, reps = 15, weight = 40.0),

        // Arm Exercises
        Exercise(name = "Bicep Curls", category = ExerciseCategory.ARMS, sets = 3, reps = 12, weight = 15.0),
        Exercise(name = "Tricep Extensions", category = ExerciseCategory.ARMS, sets = 3, reps = 12, weight = 20.0),
        Exercise(name = "Hammer Curls", category = ExerciseCategory.ARMS, sets = 3, reps = 12, weight = 12.0),
        Exercise(name = "Skull Crushers", category = ExerciseCategory.ARMS, sets = 3, reps = 10, weight = 25.0),

        // Core Exercises
        Exercise(name = "Plank", category = ExerciseCategory.CORE, sets = 3, reps = 1, weight = 0.0, notes = "Hold for 60 seconds"),
        Exercise(name = "Russian Twists", category = ExerciseCategory.CORE, sets = 3, reps = 20, weight = 10.0),
        Exercise(name = "Leg Raises", category = ExerciseCategory.CORE, sets = 3, reps = 15, weight = 0.0),
        Exercise(name = "Crunches", category = ExerciseCategory.CORE, sets = 3, reps = 20, weight = 0.0),

        // Cardio Exercises
        Exercise(name = "Running", category = ExerciseCategory.CARDIO, sets = 1, reps = 1, weight = 0.0, notes = "30 minutes"),
        Exercise(name = "Cycling", category = ExerciseCategory.CARDIO, sets = 1, reps = 1, weight = 0.0, notes = "45 minutes"),
        Exercise(name = "Jump Rope", category = ExerciseCategory.CARDIO, sets = 3, reps = 1, weight = 0.0, notes = "5 minutes per set")
    )

    fun getDefaultExercises(): List<Exercise> {
        return defaultExercises
    }

    fun getExercisesByCategory(category: ExerciseCategory): List<Exercise> {
        return defaultExercises.filter { it.category == category }
    }

    fun searchExercises(query: String): List<Exercise> {
        return defaultExercises.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.category.getDisplayName().contains(query, ignoreCase = true)
        }
    }

    fun getPopularExercises(): List<Exercise> {
        return listOf(
            defaultExercises[0], // Bench Press
            defaultExercises[4], // Deadlift
            defaultExercises[8], // Squats
            defaultExercises[12], // Shoulder Press
            defaultExercises[16] // Bicep Curls
        )
    }

    fun createCustomExercise(
        name: String,
        category: ExerciseCategory,
        sets: Int = 3,
        reps: Int = 10,
        weight: Double = 0.0,
        notes: String = ""
    ): Exercise {
        return Exercise(
            name = name,
            category = category,
            sets = sets,
            reps = reps,
            weight = weight,
            notes = notes
        )
    }

    fun calculateOneRepMax(weight: Double, reps: Int): Double {
        return weight * (1 + reps / 30.0)
    }

    fun calculateTrainingVolume(exercises: List<Exercise>): Double {
        return exercises.sumOf { it.sets * it.reps * it.weight }
    }

    fun getRecommendedWeight(exercise: Exercise, experienceLevel: String): Double {
        return when (experienceLevel) {
            "beginner" -> exercise.weight * 0.5
            "intermediate" -> exercise.weight * 0.75
            "advanced" -> exercise.weight
            else -> exercise.weight * 0.6
        }
    }
}
