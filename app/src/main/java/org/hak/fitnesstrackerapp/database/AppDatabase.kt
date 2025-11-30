package org.hak.fitnesstrackerapp.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.hak.fitnesstrackerapp.FitnessTrackerApp
import org.hak.fitnesstrackerapp.activities.LoginActivity
import org.hak.fitnesstrackerapp.models.Converters
import org.hak.fitnesstrackerapp.models.Workout

@Database(
    entities = [Workout::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun userDao()

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(activity: LoginActivity): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    FitnessTrackerApp.instance,
                    AppDatabase::class.java,
                    "fitness_tracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
