package org.hak.fitnesstrackerapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
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
