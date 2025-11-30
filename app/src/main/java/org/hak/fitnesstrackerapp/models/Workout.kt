package org.hak.fitnesstrackerapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.io.Serializable
import java.util.Date

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val type: WorkoutType,
    val duration: Int,
    val calories: Int,
    val date: Date,
    val distance: Double? = null,
    val averageSpeed: Double? = null,
    val notes: String? = null
) : Serializable

enum class WorkoutType {
    RUNNING, CYCLING, WEIGHTLIFTING;

    override fun toString(): String {
        return when (this) {
            RUNNING -> "RUNNING"
            CYCLING -> "CYCLING"
            WEIGHTLIFTING -> "WEIGHTLIFTING"
        }
    }

    companion object {
        fun fromString(value: String): WorkoutType {
            return when (value.uppercase()) {
                "RUNNING" -> RUNNING
                "CYCLING" -> CYCLING
                "WEIGHTLIFTING" -> WEIGHTLIFTING
                else -> RUNNING // default
            }
        }
    }
}

// Converters for Room
class Converters {
    @TypeConverter
    fun fromWorkoutType(type: WorkoutType): String {
        return type.toString()
    }

    @TypeConverter
    fun toWorkoutType(value: String): WorkoutType {
        return WorkoutType.fromString(value)
    }

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun toDate(timestamp: Long): Date {
        return Date(timestamp)
    }
}
