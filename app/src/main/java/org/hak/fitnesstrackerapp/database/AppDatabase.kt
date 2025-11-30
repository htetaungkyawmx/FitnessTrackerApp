package org.hak.fitnesstrackerapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase // FIXED: Add import
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.managers.ExerciseManager
import org.hak.fitnesstrackerapp.models.Exercise
import org.hak.fitnesstrackerapp.models.Goal
import org.hak.fitnesstrackerapp.models.User
import org.hak.fitnesstrackerapp.models.Workout
import java.util.concurrent.Executors

@Database(
    entities = [User::class, Workout::class, Goal::class, Exercise::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun goalDao(): GoalDao
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_tracker_database"
                )
                    .addCallback(databaseCallback)
                    .setQueryExecutor(Executors.newFixedThreadPool(4))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val databaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) { // FIXED: Use correct type
                super.onCreate(db)
                // Pre-populate with default exercises
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.exerciseDao().insertAll(ExerciseManager.getDefaultExercises())
                    }
                }
            }
        }
    }
}
