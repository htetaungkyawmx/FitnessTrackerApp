package org.hak.fitnesstrackerapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: ExerciseCategory,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val notes: String? = null
) {
    fun getTotalVolume(): Double {
        return sets * reps * weight
    }
}

// Add ExerciseCategory converter if you're storing exercises in Room database
class ExerciseCategoryConverter {
    @androidx.room.TypeConverter
    fun fromExerciseCategory(category: ExerciseCategory): String {
        return category.name
    }

    @androidx.room.TypeConverter
    fun toExerciseCategory(value: String): ExerciseCategory {
        return ExerciseCategory.valueOf(value)
    }
}
