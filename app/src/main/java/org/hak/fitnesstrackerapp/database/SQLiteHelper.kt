package org.hak.fitnesstrackerapp.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.hak.fitnesstrackerapp.model.Goal
import org.hak.fitnesstrackerapp.model.User
import org.hak.fitnesstrackerapp.model.Workout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SQLiteHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "fitness_tracker.db"
        private const val DATABASE_VERSION = 2
        const val TABLE_USERS = "users"
        const val COL_USER_ID = "id"
        const val COL_USER_NAME = "name"
        const val COL_USER_EMAIL = "email"
        const val COL_USER_PASSWORD = "password"
        const val COL_USER_AGE = "age"
        const val COL_USER_WEIGHT = "weight"
        const val COL_USER_HEIGHT = "height"
        const val COL_USER_CREATED_AT = "created_at"

        const val TABLE_WORKOUTS = "workouts"
        const val COL_ID = "id"
        const val COL_WORKOUT_USER_ID = "user_id"
        const val COL_TYPE = "type"
        const val COL_DURATION = "duration"
        const val COL_DISTANCE = "distance"
        const val COL_CALORIES = "calories"
        const val COL_NOTES = "notes"
        const val COL_DATE = "date"
        const val COL_TIMESTAMP = "timestamp"
        const val COL_SYNCED = "synced"

        const val TABLE_GOALS = "goals"
        const val COL_GOAL_ID = "id"
        const val COL_GOAL_USER_ID = "user_id"
        const val COL_GOAL_TYPE = "type"
        const val COL_GOAL_TARGET = "target"
        const val COL_GOAL_CURRENT = "progress"
        const val COL_GOAL_DEADLINE = "deadline"
        const val COL_GOAL_ACHIEVED = "achieved"
        const val COL_GOAL_CREATED_AT = "created_at"

        private val TAG = "SQLiteHelper"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_NAME TEXT NOT NULL,
                $COL_USER_EMAIL TEXT UNIQUE NOT NULL,
                $COL_USER_PASSWORD TEXT NOT NULL,
                $COL_USER_AGE INTEGER,
                $COL_USER_WEIGHT REAL,
                $COL_USER_HEIGHT REAL,
                $COL_USER_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()

        val createWorkoutsTable = """
            CREATE TABLE $TABLE_WORKOUTS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_WORKOUT_USER_ID INTEGER NOT NULL,
                $COL_TYPE TEXT NOT NULL,
                $COL_DURATION INTEGER NOT NULL,
                $COL_DISTANCE REAL,
                $COL_CALORIES INTEGER NOT NULL,
                $COL_NOTES TEXT,
                $COL_DATE TEXT NOT NULL,
                $COL_TIMESTAMP INTEGER NOT NULL,
                $COL_SYNCED INTEGER DEFAULT 0,
                FOREIGN KEY($COL_WORKOUT_USER_ID) 
                REFERENCES $TABLE_USERS($COL_USER_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        val createGoalsTable = """
            CREATE TABLE $TABLE_GOALS (
                $COL_GOAL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_GOAL_USER_ID INTEGER NOT NULL,
                $COL_GOAL_TYPE TEXT NOT NULL,
                $COL_GOAL_TARGET REAL NOT NULL,
                $COL_GOAL_CURRENT REAL DEFAULT 0,
                $COL_GOAL_DEADLINE TEXT NOT NULL,
                $COL_GOAL_ACHIEVED INTEGER DEFAULT 0,
                $COL_GOAL_CREATED_AT INTEGER NOT NULL,
                FOREIGN KEY($COL_GOAL_USER_ID) 
                REFERENCES $TABLE_USERS($COL_USER_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createWorkoutsTable)
        db.execSQL(createGoalsTable)

        Log.d(TAG, "Database tables created successfully")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        when (oldVersion) {
            1 -> {
                try {
                    val createGoalsTable = """
                        CREATE TABLE $TABLE_GOALS (
                            $COL_GOAL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                            $COL_GOAL_USER_ID INTEGER NOT NULL,
                            $COL_GOAL_TYPE TEXT NOT NULL,
                            $COL_GOAL_TARGET REAL NOT NULL,
                            $COL_GOAL_CURRENT REAL DEFAULT 0,
                            $COL_GOAL_DEADLINE TEXT NOT NULL,
                            $COL_GOAL_ACHIEVED INTEGER DEFAULT 0,
                            $COL_GOAL_CREATED_AT INTEGER NOT NULL,
                            FOREIGN KEY($COL_GOAL_USER_ID) 
                            REFERENCES $TABLE_USERS($COL_USER_ID) ON DELETE CASCADE
                        )
                    """.trimIndent()

                    db.execSQL(createGoalsTable)
                    Log.d(TAG, "Goals table added in migration")
                } catch (e: Exception) {
                    Log.e(TAG, "Error upgrading database: ${e.message}")
                }
            }
        }
    }
    fun getRecentWorkoutsByType(userId: Int, workoutType: String, days: Int): List<Workout> {
        val workouts = mutableListOf<Workout>()
        val db = this.readableDatabase

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = dateFormat.format(calendar.time)

        val query = """
            SELECT * FROM $TABLE_WORKOUTS 
            WHERE $COL_WORKOUT_USER_ID = ? 
            AND $COL_TYPE = ? 
            AND $COL_DATE >= ?
            ORDER BY $COL_DATE DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), workoutType, startDate))

        try {
            while (cursor.moveToNext()) {
                workouts.add(getWorkoutFromCursor(cursor))
            }
            Log.d(TAG, "Found ${workouts.size} recent workouts for type: $workoutType")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent workouts: ${e.message}")
        } finally {
            cursor.close()
            db.close()
        }

        return workouts
    }
    fun hasWorkoutsByType(userId: Int, workoutType: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM workouts WHERE user_id = ? AND type = ?",
            arrayOf(userId.toString(), workoutType)
        )

        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        return count > 0
    }

    fun getWorkoutsByDateRange(userId: Int, startDate: String, endDate: String): List<Workout> {
        val workouts = mutableListOf<Workout>()
        val db = this.readableDatabase

        val query = """
            SELECT * FROM $TABLE_WORKOUTS 
            WHERE $COL_WORKOUT_USER_ID = ? 
            AND $COL_DATE BETWEEN ? AND ?
            ORDER BY $COL_DATE DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), startDate, endDate))

        try {
            while (cursor.moveToNext()) {
                workouts.add(getWorkoutFromCursor(cursor))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting workouts by date range: ${e.message}")
        } finally {
            cursor.close()
            db.close()
        }

        return workouts
    }

    fun getWorkoutsByType(userId: Int, workoutType: String): List<Workout> {
        val workouts = mutableListOf<Workout>()
        val db = this.readableDatabase

        val query = """
            SELECT * FROM $TABLE_WORKOUTS 
            WHERE $COL_WORKOUT_USER_ID = ? 
            AND $COL_TYPE = ?
            ORDER BY $COL_DATE DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), workoutType))

        try {
            while (cursor.moveToNext()) {
                workouts.add(getWorkoutFromCursor(cursor))
            }
            Log.d(TAG, "Found ${workouts.size} workouts for type: $workoutType")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting workouts by type: ${e.message}")
        } finally {
            cursor.close()
            db.close()
        }

        return workouts
    }

    fun getLastWorkoutByType(userId: Int, workoutType: String): Workout? {
        val db = this.readableDatabase

        val query = """
            SELECT * FROM $TABLE_WORKOUTS 
            WHERE $COL_WORKOUT_USER_ID = ? 
            AND $COL_TYPE = ?
            ORDER BY $COL_DATE DESC, $COL_TIMESTAMP DESC 
            LIMIT 1
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), workoutType))

        return try {
            if (cursor.moveToFirst()) {
                getWorkoutFromCursor(cursor)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last workout: ${e.message}")
            null
        } finally {
            cursor.close()
            db.close()
        }
    }

    fun getWorkoutCountByType(userId: Int, workoutType: String): Int {
        val db = this.readableDatabase

        val query = """
            SELECT COUNT(*) FROM $TABLE_WORKOUTS 
            WHERE $COL_WORKOUT_USER_ID = ? 
            AND $COL_TYPE = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), workoutType))

        return try {
            if (cursor.moveToFirst()) {
                cursor.getInt(0)
            } else {
                0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting workout count: ${e.message}")
            0
        } finally {
            cursor.close()
            db.close()
        }
    }

    fun getTodayWorkouts(userId: Int): List<Workout> {
        val workouts = mutableListOf<Workout>()
        val db = this.readableDatabase

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val query = """
            SELECT * FROM $TABLE_WORKOUTS 
            WHERE $COL_WORKOUT_USER_ID = ? 
            AND $COL_DATE = ?
            ORDER BY $COL_TIMESTAMP DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), today))

        try {
            while (cursor.moveToNext()) {
                workouts.add(getWorkoutFromCursor(cursor))
            }
            Log.d(TAG, "Found ${workouts.size} workouts for today")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting today's workouts: ${e.message}")
        } finally {
            cursor.close()
            db.close()
        }

        return workouts
    }

    fun getMonthlyWorkouts(userId: Int, year: Int, month: Int): List<Workout> {
        val workouts = mutableListOf<Workout>()
        val db = this.readableDatabase

        val monthStr = String.format("%04d-%02d", year, month)

        val query = """
            SELECT * FROM $TABLE_WORKOUTS 
            WHERE $COL_WORKOUT_USER_ID = ? 
            AND $COL_DATE LIKE '${monthStr}%'
            ORDER BY $COL_DATE DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        try {
            while (cursor.moveToNext()) {
                workouts.add(getWorkoutFromCursor(cursor))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting monthly workouts: ${e.message}")
        } finally {
            cursor.close()
            db.close()
        }

        return workouts
    }

    private fun getWorkoutFromCursor(cursor: Cursor): Workout {
        return Workout(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_WORKOUT_USER_ID)),
            type = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)),
            duration = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DURATION)),
            distance = if (cursor.isNull(cursor.getColumnIndexOrThrow(COL_DISTANCE))) null
            else cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DISTANCE)),
            calories = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CALORIES)),
            notes = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES)),
            date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
            timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)),
            synced = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SYNCED)) == 1
        )
    }

    fun deleteWorkout(workoutId: Int): Boolean {
        val db = this.writableDatabase

        return try {
            val result = db.delete(
                TABLE_WORKOUTS,
                "$COL_ID = ?",
                arrayOf(workoutId.toString())
            )
            db.close()
            result > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting workout: ${e.message}")
            db.close()
            false
        }
    }

    fun updateWorkout(workout: Workout): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COL_TYPE, workout.type)
        values.put(COL_DURATION, workout.duration)
        values.put(COL_DISTANCE, workout.distance)
        values.put(COL_CALORIES, workout.calories)
        values.put(COL_NOTES, workout.notes)
        values.put(COL_DATE, workout.date)
        values.put(COL_SYNCED, if (workout.synced) 1 else 0)

        return try {
            val result = db.update(
                TABLE_WORKOUTS,
                values,
                "$COL_ID = ?",
                arrayOf(workout.id.toString())
            )
            db.close()
            result > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error updating workout: ${e.message}")
            db.close()
            false
        }
    }

    fun getWorkoutStatisticsByType(userId: Int, workoutType: String): Map<String, Any> {
        val db = this.readableDatabase
        val stats = mutableMapOf<String, Any>()

        val query = """
            SELECT 
                COUNT(*) as count,
                SUM($COL_DURATION) as total_duration,
                SUM($COL_CALORIES) as total_calories,
                AVG($COL_DURATION) as avg_duration,
                AVG($COL_CALORIES) as avg_calories
            FROM $TABLE_WORKOUTS 
            WHERE $COL_WORKOUT_USER_ID = ? 
            AND $COL_TYPE = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), workoutType))

        try {
            if (cursor.moveToFirst()) {
                stats["count"] = cursor.getInt(0)
                stats["total_duration"] = cursor.getInt(1)
                stats["total_calories"] = cursor.getInt(2)
                stats["avg_duration"] = cursor.getDouble(3)
                stats["avg_calories"] = cursor.getDouble(4)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting workout statistics: ${e.message}")
        } finally {
            cursor.close()
            db.close()
        }

        return stats
    }

    fun hasRecentWorkoutsForGPS(userId: Int, workoutType: String): Boolean {
        val recentWorkouts = getRecentWorkoutsByType(userId, workoutType, 30)
        return recentWorkouts.isNotEmpty()
    }

    fun getWorkoutTypesWithCounts(userId: Int): Map<String, Int> {
        val typeCounts = mutableMapOf<String, Int>()
        val db = this.readableDatabase

        val query = """
            SELECT $COL_TYPE, COUNT(*) as count 
            FROM $TABLE_WORKOUTS 
            WHERE $COL_WORKOUT_USER_ID = ? 
            GROUP BY $COL_TYPE 
            ORDER BY count DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        try {
            while (cursor.moveToNext()) {
                val type = cursor.getString(0)
                val count = cursor.getInt(1)
                typeCounts[type] = count
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting workout types: ${e.message}")
        } finally {
            cursor.close()
            db.close()
        }

        return typeCounts
    }

    fun getAllWorkoutTypes(userId: Int): List<String> {
        val types = mutableListOf<String>()
        val db = this.readableDatabase

        val query = """
            SELECT DISTINCT $COL_TYPE 
            FROM $TABLE_WORKOUTS 
            WHERE $COL_WORKOUT_USER_ID = ? 
            ORDER BY $COL_TYPE
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        try {
            while (cursor.moveToNext()) {
                types.add(cursor.getString(0))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting workout types: ${e.message}")
        } finally {
            cursor.close()
            db.close()
        }

        return types
    }
    fun insertUser(user: User): Long {
        val db = this.writableDatabase
        val values = ContentValues()

        val password = user.password ?: ""

        Log.d(TAG, "Inserting user: ${user.email}, Password length: ${password.length}")

        values.put(COL_USER_NAME, user.name)
        values.put(COL_USER_EMAIL, user.email)
        values.put(COL_USER_PASSWORD, password)
        values.put(COL_USER_AGE, user.age)
        values.put(COL_USER_WEIGHT, user.weight)
        values.put(COL_USER_HEIGHT, user.height)
        values.put(COL_USER_CREATED_AT, Date().time)

        try {
            val id = db.insert(TABLE_USERS, null, values)
            Log.d(TAG, "User inserted with ID: $id")
            return id
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting user: ${e.message}")
            e.printStackTrace()
            return -1
        } finally {
            db.close()
        }
    }

    fun getUserByEmail(email: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COL_USER_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )

        return if (cursor != null && cursor.moveToFirst()) {
            val user = User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD)),
                age = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_AGE)),
                weight = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_USER_WEIGHT)),
                height = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_USER_HEIGHT))
            )
            cursor.close()
            db.close()
            user
        } else {
            cursor?.close()
            db.close()
            null
        }
    }

    fun getAllUsers(): List<User> {
        val users = ArrayList<User>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            null,
            null,
            null,
            null,
            "$COL_USER_CREATED_AT DESC"
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val user = User(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)),
                    password = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD)),
                    age = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_AGE)),
                    weight = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_USER_WEIGHT)),
                    height = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_USER_HEIGHT))
                )
                users.add(user)
            } while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        return users
    }

    fun userExists(email: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COL_USER_EMAIL = ?",
            arrayOf(email)
        )

        val exists = cursor != null && cursor.count > 0
        cursor?.close()
        db.close()
        return exists
    }

    fun authenticateUser(email: String, password: String): User? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COL_USER_EMAIL = ? AND $COL_USER_PASSWORD = ?",
            arrayOf(email, password)
        )

        return if (cursor != null && cursor.moveToFirst()) {
            val user = User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD)),
                age = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_AGE)),
                weight = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_USER_WEIGHT)),
                height = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_USER_HEIGHT))
            )
            cursor.close()
            db.close()
            user
        } else {
            cursor?.close()
            db.close()
            null
        }
    }

    fun updateUser(user: User): Int {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COL_USER_NAME, user.name)
        values.put(COL_USER_EMAIL, user.email)
        values.put(COL_USER_PASSWORD, user.password ?: "")
        values.put(COL_USER_AGE, user.age)
        values.put(COL_USER_WEIGHT, user.weight)
        values.put(COL_USER_HEIGHT, user.height)

        val rowsAffected = db.update(
            TABLE_USERS,
            values,
            "$COL_USER_ID = ?",
            arrayOf(user.id.toString())
        )
        db.close()
        return rowsAffected
    }

    fun deleteUser(userId: Int): Int {
        val db = this.writableDatabase
        val rowsAffected = db.delete(
            TABLE_USERS,
            "$COL_USER_ID = ?",
            arrayOf(userId.toString())
        )
        db.close()
        return rowsAffected
    }

    fun insertWorkout(workout: Workout): Long {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COL_WORKOUT_USER_ID, workout.userId)
        values.put(COL_TYPE, workout.type)
        values.put(COL_DURATION, workout.duration)

        if (workout.distance != null) {
            values.put(COL_DISTANCE, workout.distance)
        }

        values.put(COL_CALORIES, workout.calories)
        values.put(COL_NOTES, workout.notes)
        values.put(COL_DATE, workout.date)
        values.put(COL_TIMESTAMP, Date().time)
        values.put(COL_SYNCED, if (workout.synced) 1 else 0)

        val id = db.insert(TABLE_WORKOUTS, null, values)
        db.close()
        return id
    }

    fun getAllWorkouts(userId: Int): List<Workout> {
        val workouts = ArrayList<Workout>()
        val db = this.readableDatabase

        val selection = "$COL_WORKOUT_USER_ID = ?"
        val selectionArgs = arrayOf(userId.toString())

        val cursor = db.query(
            TABLE_WORKOUTS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "$COL_DATE DESC, $COL_TIMESTAMP DESC"
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val workout = Workout(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_WORKOUT_USER_ID)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)),
                    duration = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DURATION)),
                    distance = if (cursor.isNull(cursor.getColumnIndexOrThrow(COL_DISTANCE))) null
                    else cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DISTANCE)),
                    calories = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CALORIES)),
                    notes = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)),
                    synced = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SYNCED)) == 1
                )
                workouts.add(workout)
            } while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        return workouts
    }

    fun getUnsyncedWorkouts(userId: Int): List<Workout> {
        val workouts = ArrayList<Workout>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_WORKOUTS,
            null,
            "$COL_SYNCED = ? AND $COL_WORKOUT_USER_ID = ?",
            arrayOf("0", userId.toString()),
            null,
            null,
            "$COL_TIMESTAMP ASC"
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val workout = Workout(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_WORKOUT_USER_ID)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)),
                    duration = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DURATION)),
                    distance = if (cursor.isNull(cursor.getColumnIndexOrThrow(COL_DISTANCE))) null
                    else cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DISTANCE)),
                    calories = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CALORIES)),
                    notes = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)),
                    synced = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SYNCED)) == 1
                )
                workouts.add(workout)
            } while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        return workouts
    }

    fun getDailyDuration(userId: Int, date: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COL_DURATION) FROM $TABLE_WORKOUTS WHERE $COL_DATE = ? AND $COL_WORKOUT_USER_ID = ?",
            arrayOf(date, userId.toString())
        )

        var duration = 0
        if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
            duration = cursor.getInt(0)
        }
        cursor?.close()
        db.close()
        return duration
    }

    fun getWeeklyCalories(userId: Int, startDate: String, endDate: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COL_CALORIES) FROM $TABLE_WORKOUTS WHERE $COL_DATE BETWEEN ? AND ? AND $COL_WORKOUT_USER_ID = ?",
            arrayOf(startDate, endDate, userId.toString())
        )

        var calories = 0
        if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
            calories = cursor.getInt(0)
        }
        cursor?.close()
        db.close()
        return calories
    }

    fun updateWorkoutSyncStatus(workoutId: Int, synced: Boolean) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_SYNCED, if (synced) 1 else 0)

        db.update(
            TABLE_WORKOUTS,
            values,
            "$COL_ID = ?",
            arrayOf(workoutId.toString())
        )
        db.close()
    }

    fun getWorkoutById(workoutId: Int): Workout? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_WORKOUTS,
            null,
            "$COL_ID = ?",
            arrayOf(workoutId.toString()),
            null,
            null,
            null
        )

        return if (cursor != null && cursor.moveToFirst()) {
            val workout = Workout(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_WORKOUT_USER_ID)),
                type = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)),
                duration = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DURATION)),
                distance = if (cursor.isNull(cursor.getColumnIndexOrThrow(COL_DISTANCE))) null
                else cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DISTANCE)),
                calories = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CALORIES)),
                notes = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES)),
                date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)),
                synced = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SYNCED)) == 1
            )
            cursor.close()
            db.close()
            workout
        } else {
            cursor?.close()
            db.close()
            null
        }
    }

    fun deleteAllWorkouts() {
        val db = this.writableDatabase
        db.delete(TABLE_WORKOUTS, null, null)
        db.close()
    }

    fun insertGoal(goal: Goal): Long {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COL_GOAL_USER_ID, goal.userId)
        values.put(COL_GOAL_TYPE, goal.type)
        values.put(COL_GOAL_TARGET, goal.target)
        values.put(COL_GOAL_CURRENT, goal.current)
        values.put(COL_GOAL_DEADLINE, goal.deadline)
        values.put(COL_GOAL_ACHIEVED, if (goal.achieved) 1 else 0)
        values.put(COL_GOAL_CREATED_AT, System.currentTimeMillis())

        val id = db.insert(TABLE_GOALS, null, values)
        db.close()
        return id
    }

    fun getAllGoals(userId: Int): List<Goal> {
        val goals = ArrayList<Goal>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_GOALS,
            null,
            "$COL_GOAL_USER_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            "$COL_GOAL_DEADLINE ASC, $COL_GOAL_CREATED_AT DESC"
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val goal = Goal(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_GOAL_ID)),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_GOAL_USER_ID)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(COL_GOAL_TYPE)),
                    target = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_GOAL_TARGET)),
                    current = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_GOAL_CURRENT)),
                    deadline = cursor.getString(cursor.getColumnIndexOrThrow(COL_GOAL_DEADLINE)),
                    achieved = cursor.getInt(cursor.getColumnIndexOrThrow(COL_GOAL_ACHIEVED)) == 1
                )
                goals.add(goal)
            } while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        return goals
    }

    fun getGoalById(goalId: Int): Goal? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_GOALS,
            null,
            "$COL_GOAL_ID = ?",
            arrayOf(goalId.toString()),
            null,
            null,
            null
        )

        return if (cursor != null && cursor.moveToFirst()) {
            val goal = Goal(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_GOAL_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_GOAL_USER_ID)),
                type = cursor.getString(cursor.getColumnIndexOrThrow(COL_GOAL_TYPE)),
                target = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_GOAL_TARGET)),
                current = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_GOAL_CURRENT)),
                deadline = cursor.getString(cursor.getColumnIndexOrThrow(COL_GOAL_DEADLINE)),
                achieved = cursor.getInt(cursor.getColumnIndexOrThrow(COL_GOAL_ACHIEVED)) == 1
            )
            cursor.close()
            db.close()
            goal
        } else {
            cursor?.close()
            db.close()
            null
        }
    }

    fun updateGoalProgress(goalId: Int, current: Double) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_GOAL_CURRENT, current)

        val cursor = db.query(
            TABLE_GOALS,
            arrayOf(COL_GOAL_TARGET),
            "$COL_GOAL_ID = ?",
            arrayOf(goalId.toString()),
            null, null, null
        )

        if (cursor != null && cursor.moveToFirst()) {
            val target = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_GOAL_TARGET))
            if (current >= target) {
                values.put(COL_GOAL_ACHIEVED, 1)
            }
            cursor.close()
        }

        db.update(
            TABLE_GOALS,
            values,
            "$COL_GOAL_ID = ?",
            arrayOf(goalId.toString())
        )
        db.close()
    }

    fun updateGoal(goal: Goal): Int {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COL_GOAL_TYPE, goal.type)
        values.put(COL_GOAL_TARGET, goal.target)
        values.put(COL_GOAL_CURRENT, goal.current)
        values.put(COL_GOAL_DEADLINE, goal.deadline)
        values.put(COL_GOAL_ACHIEVED, if (goal.achieved) 1 else 0)

        val rowsAffected = db.update(
            TABLE_GOALS,
            values,
            "$COL_GOAL_ID = ?",
            arrayOf(goal.id.toString())
        )
        db.close()
        return rowsAffected
    }

    fun deleteGoal(goalId: Int): Int {
        val db = this.writableDatabase
        val rowsAffected = db.delete(
            TABLE_GOALS,
            "$COL_GOAL_ID = ?",
            arrayOf(goalId.toString())
        )
        db.close()
        return rowsAffected
    }

    fun getTotalWorkoutsCount(userId: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_WORKOUTS WHERE $COL_WORKOUT_USER_ID = ?",
            arrayOf(userId.toString())
        )

        var count = 0
        if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
            count = cursor.getInt(0)
        }
        cursor?.close()
        db.close()
        return count
    }

    fun getTotalDuration(userId: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COL_DURATION) FROM $TABLE_WORKOUTS WHERE $COL_WORKOUT_USER_ID = ?",
            arrayOf(userId.toString())
        )

        var totalDuration = 0
        if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
            totalDuration = cursor.getInt(0)
        }
        cursor?.close()
        db.close()
        return totalDuration
    }

    fun getTotalCalories(userId: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COL_CALORIES) FROM $TABLE_WORKOUTS WHERE $COL_WORKOUT_USER_ID = ?",
            arrayOf(userId.toString())
        )

        var totalCalories = 0
        if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
            totalCalories = cursor.getInt(0)
        }
        cursor?.close()
        db.close()
        return totalCalories
    }

    fun closeDB() {
        this.close()
    }

    private fun Cursor.getColumnIndexOrThrow(columnName: String): Int {
        val index = this.getColumnIndex(columnName)
        if (index == -1) {
            throw IllegalArgumentException("Column $columnName does not exist")
        }
        return index
    }
}