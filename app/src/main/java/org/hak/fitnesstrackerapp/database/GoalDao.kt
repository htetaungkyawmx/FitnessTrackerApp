package org.hak.fitnesstrackerapp.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.hak.fitnesstrackerapp.models.Goal

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY deadline ASC")
    fun getGoalsByUser(userId: Int): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE userId = :userId AND type = :type ORDER BY deadline ASC")
    fun getGoalsByType(userId: Int, type: String): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE userId = :userId AND isCompleted = :isCompleted ORDER BY deadline ASC")
    fun getGoalsByCompletionStatus(userId: Int, isCompleted: Boolean): Flow<List<Goal>>

    @Insert
    suspend fun insertGoal(goal: Goal)

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId AND isCompleted = 1")
    suspend fun getCompletedGoalsCount(userId: Int): Int

    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId")
    suspend fun getTotalGoalsCount(userId: Int): Int

    @Query("SELECT * FROM goals WHERE userId = :userId AND deadline < :currentDate AND isCompleted = 0")
    suspend fun getOverdueGoals(userId: Int, currentDate: java.util.Date): List<Goal>

    @Query("DELETE FROM goals")
    suspend fun clearAll()
}
