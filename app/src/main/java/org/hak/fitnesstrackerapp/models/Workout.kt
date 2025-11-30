package org.hak.fitnesstrackerapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.hak.fitnesstrackerapp.database.Converters
import java.util.Date

@Entity(tableName = "workouts")
@TypeConverters(Converters::class)
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Int,

    val type: WorkoutType,

    val duration: Int, // in minutes

    val calories: Double,

    val date: Date,

    val notes: String = "",

    val distance: Double? = null, // for running/cycling

    val averageSpeed: Double? = null, // for running/cycling

    val elevation: Double? = null, // for cycling

    val exercises: List<Exercise> = emptyList() // for weightlifting
)
