package org.hak.fitnesstrackerapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.hak.fitnesstrackerapp.models.Workout

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY date DESC")
    fun getWorkoutsByUser(userId: Int): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE userId = :userId AND type = :type ORDER BY date DESC")
    fun getWorkoutsByType(userId: Int, type: String): Flow<List<Workout>>

    @Insert
    suspend fun insertWorkout(workout: Workout)

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Query("SELECT SUM(calories) FROM workouts WHERE userId = :userId")
    suspend fun getTotalCalories(userId: Int): Double

    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId")
    suspend fun getTotalWorkouts(userId: Int): Int

    @Query("SELECT SUM(duration) FROM workouts WHERE userId = :userId")
    suspend fun getTotalDuration(userId: Int): Int

    @Query("SELECT SUM(distance) FROM workouts WHERE userId = :userId AND type = 'RUNNING'")
    suspend fun getTotalRunningDistance(userId: Int): Double

    @Query("SELECT SUM(distance) FROM workouts WHERE userId = :userId AND type = 'CYCLING'")
    suspend fun getTotalCyclingDistance(userId: Int): Double

    @Query("DELETE FROM workouts")
    suspend fun clearAll()

    // Additional methods for statistics
    @Query("SELECT AVG(calories) FROM workouts WHERE userId = :userId")
    suspend fun getAverageCalories(userId: Int): Double

    @Query("SELECT AVG(duration) FROM workouts WHERE userId = :userId")
    suspend fun getAverageDuration(userId: Int): Double

    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    suspend fun getWorkoutsInDateRange(userId: Int, startDate: java.util.Date, endDate: java.util.Date): Int

    @Query("SELECT * FROM workouts WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getWorkoutsByDateRange(userId: Int, startDate: java.util.Date, endDate: java.util.Date): Flow<List<Workout>>
}
