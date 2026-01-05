package org.azm.fitness_app.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.azm.fitness_app.model.Goal
import org.azm.fitness_app.model.User
import org.azm.fitness_app.model.Workout
import java.util.Date

class SQLiteHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "azm_db.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USER_NAME = "name"
        const val COLUMN_USER_EMAIL = "email"
        const val COLUMN_USER_PASSWORD = "password"
        const val COLUMN_USER_AGE = "age"
        const val COLUMN_USER_WEIGHT = "weight"
        const val COLUMN_USER_HEIGHT = "height"
        const val COLUMN_USER_CREATED_AT = "created_at"

        const val TABLE_WORKOUTS = "workouts"
        const val COLUMN_ID = "id"
        const val COLUMN_WORKOUT_USER_ID = "user_id"
        const val COLUMN_TYPE = "type"
        const val COLUMN_DURATION = "duration"
        const val COLUMN_DISTANCE = "distance"
        const val COLUMN_CALORIES = "calories"
        const val COLUMN_NOTES = "notes"
        const val COLUMN_DATE = "date"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_SYNCED = "synced"

        const val TABLE_GOALS = "goals"
        const val COLUMN_GOAL_ID = "id"
        const val COLUMN_GOAL_USER_ID = "user_id"
        const val COLUMN_GOAL_TYPE = "type"
        const val COLUMN_GOAL_TARGET = "target"
        const val COLUMN_GOAL_CURRENT = "progress"
        const val COLUMN_GOAL_DEADLINE = "deadline"
        const val COLUMN_GOAL_ACHIEVED = "achieved"
        const val COLUMN_GOAL_CREATED_AT = "created_at"

        private val TAG = "SQLiteHelper"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NAME TEXT NOT NULL,
                $COLUMN_USER_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_USER_PASSWORD TEXT NOT NULL,
                $COLUMN_USER_AGE INTEGER,
                $COLUMN_USER_WEIGHT REAL,
                $COLUMN_USER_HEIGHT REAL,
                $COLUMN_USER_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()

        val createWorkoutsTable = """
            CREATE TABLE $TABLE_WORKOUTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_WORKOUT_USER_ID INTEGER NOT NULL,
                $COLUMN_TYPE TEXT NOT NULL,
                $COLUMN_DURATION INTEGER NOT NULL,
                $COLUMN_DISTANCE REAL,
                $COLUMN_CALORIES INTEGER NOT NULL,
                $COLUMN_NOTES TEXT,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_TIMESTAMP INTEGER NOT NULL,
                $COLUMN_SYNCED INTEGER DEFAULT 0,
                FOREIGN KEY($COLUMN_WORKOUT_USER_ID) 
                REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        val createGoalsTable = """
        CREATE TABLE $TABLE_GOALS (
            $COLUMN_GOAL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_GOAL_USER_ID INTEGER NOT NULL,
            $COLUMN_GOAL_TYPE TEXT NOT NULL,
            $COLUMN_GOAL_TARGET REAL NOT NULL,
            $COLUMN_GOAL_CURRENT REAL DEFAULT 0, 
            $COLUMN_GOAL_DEADLINE TEXT NOT NULL,
            $COLUMN_GOAL_ACHIEVED INTEGER DEFAULT 0,
            $COLUMN_GOAL_CREATED_AT INTEGER NOT NULL,
            FOREIGN KEY($COLUMN_GOAL_USER_ID) 
            REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
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
                            $COLUMN_GOAL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                            $COLUMN_GOAL_USER_ID INTEGER NOT NULL,
                            $COLUMN_GOAL_TYPE TEXT NOT NULL,
                            $COLUMN_GOAL_TARGET REAL NOT NULL,
                            $COLUMN_GOAL_CURRENT REAL DEFAULT 0,
                            $COLUMN_GOAL_DEADLINE TEXT NOT NULL,
                            $COLUMN_GOAL_ACHIEVED INTEGER DEFAULT 0,
                            $COLUMN_GOAL_CREATED_AT INTEGER NOT NULL,
                            FOREIGN KEY($COLUMN_GOAL_USER_ID) 
                            REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
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

    fun insertUser(user: User): Long {
        val db = this.writableDatabase
        val values = ContentValues()

        // FIX: Ensure password is not null
        val password = user.password ?: ""

        Log.d(TAG, "Inserting user: ${user.email}, Password length: ${password.length}")

        values.put(COLUMN_USER_NAME, user.name)
        values.put(COLUMN_USER_EMAIL, user.email)
        values.put(COLUMN_USER_PASSWORD, password)
        values.put(COLUMN_USER_AGE, user.age)
        values.put(COLUMN_USER_WEIGHT, user.weight)
        values.put(COLUMN_USER_HEIGHT, user.height)
        values.put(COLUMN_USER_CREATED_AT, Date().time)

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
            "$COLUMN_USER_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )

        return if (cursor != null && cursor.moveToFirst()) {
            val user = User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)),
                age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_AGE)),
                weight = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_USER_WEIGHT)),
                height = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_USER_HEIGHT))
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
            "$COLUMN_USER_CREATED_AT DESC"
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val user = User(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                    password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)),
                    age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_AGE)),
                    weight = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_USER_WEIGHT)),
                    height = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_USER_HEIGHT))
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
            "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_EMAIL = ?",
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
            "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASSWORD = ?",
            arrayOf(email, password)
        )

        return if (cursor != null && cursor.moveToFirst()) {
            val user = User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)),
                age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_AGE)),
                weight = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_USER_WEIGHT)),
                height = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_USER_HEIGHT))
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

        values.put(COLUMN_USER_NAME, user.name)
        values.put(COLUMN_USER_EMAIL, user.email)
        values.put(COLUMN_USER_PASSWORD, user.password ?: "")
        values.put(COLUMN_USER_AGE, user.age)
        values.put(COLUMN_USER_WEIGHT, user.weight)
        values.put(COLUMN_USER_HEIGHT, user.height)

        val rowsAffected = db.update(
            TABLE_USERS,
            values,
            "$COLUMN_USER_ID = ?",
            arrayOf(user.id.toString())
        )
        db.close()
        return rowsAffected
    }

    fun deleteUser(userId: Int): Int {
        val db = this.writableDatabase
        val rowsAffected = db.delete(
            TABLE_USERS,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString())
        )
        db.close()
        return rowsAffected
    }

    fun insertWorkout(workout: Workout): Long {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COLUMN_WORKOUT_USER_ID, workout.userId)
        values.put(COLUMN_TYPE, workout.type)
        values.put(COLUMN_DURATION, workout.duration)

        if (workout.distance != null) {
            values.put(COLUMN_DISTANCE, workout.distance)
        }

        values.put(COLUMN_CALORIES, workout.calories)
        values.put(COLUMN_NOTES, workout.notes)
        values.put(COLUMN_DATE, workout.date)
        values.put(COLUMN_TIMESTAMP, Date().time)
        values.put(COLUMN_SYNCED, if (workout.synced) 1 else 0)

        val id = db.insert(TABLE_WORKOUTS, null, values)
        db.close()
        return id
    }

    fun getAllWorkouts(): List<Workout> {
        val workouts = ArrayList<Workout>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_WORKOUTS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_DATE DESC, $COLUMN_TIMESTAMP DESC"
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val workout = Workout(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WORKOUT_USER_ID)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                    duration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                    distance = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE))) null
                    else cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE)),
                    calories = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIES)),
                    notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                    synced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYNCED)) == 1
                )
                workouts.add(workout)
            } while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        return workouts
    }

    fun getUnsyncedWorkouts(): List<Workout> {
        val workouts = ArrayList<Workout>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_WORKOUTS,
            null,
            "$COLUMN_SYNCED = ?",
            arrayOf("0"),
            null,
            null,
            "$COLUMN_TIMESTAMP ASC"
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val workout = Workout(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WORKOUT_USER_ID)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                    duration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                    distance = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE))) null
                    else cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE)),
                    calories = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIES)),
                    notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                    synced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYNCED)) == 1
                )
                workouts.add(workout)
            } while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        return workouts
    }

    fun getDailyDuration(date: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COLUMN_DURATION) FROM $TABLE_WORKOUTS WHERE $COLUMN_DATE = ?",
            arrayOf(date)
        )

        var duration = 0
        if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
            duration = cursor.getInt(0)
        }
        cursor?.close()
        db.close()
        return duration
    }

    fun getWeeklyCalories(startDate: String, endDate: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COLUMN_CALORIES) FROM $TABLE_WORKOUTS WHERE $COLUMN_DATE BETWEEN ? AND ?",
            arrayOf(startDate, endDate)
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
        values.put(COLUMN_SYNCED, if (synced) 1 else 0)

        db.update(
            TABLE_WORKOUTS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(workoutId.toString())
        )
        db.close()
    }

    fun getWorkoutById(workoutId: Int): Workout? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_WORKOUTS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(workoutId.toString()),
            null,
            null,
            null
        )

        return if (cursor != null && cursor.moveToFirst()) {
            val workout = Workout(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WORKOUT_USER_ID)),
                type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                duration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                distance = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE))) null
                else cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE)),
                calories = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIES)),
                notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)),
                date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                synced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYNCED)) == 1
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

        values.put(COLUMN_GOAL_USER_ID, goal.userId)
        values.put(COLUMN_GOAL_TYPE, goal.type)
        values.put(COLUMN_GOAL_TARGET, goal.target)
        values.put(COLUMN_GOAL_CURRENT, goal.current)
        values.put(COLUMN_GOAL_DEADLINE, goal.deadline)
        values.put(COLUMN_GOAL_ACHIEVED, if (goal.achieved) 1 else 0)
        values.put(COLUMN_GOAL_CREATED_AT, System.currentTimeMillis())

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
            "$COLUMN_GOAL_USER_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            "$COLUMN_GOAL_DEADLINE ASC, $COLUMN_GOAL_CREATED_AT DESC"
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val goal = Goal(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GOAL_ID)),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GOAL_USER_ID)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_TYPE)),
                    target = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GOAL_TARGET)),
                    current = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GOAL_CURRENT)),
                    deadline = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_DEADLINE)),
                    achieved = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GOAL_ACHIEVED)) == 1
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
            "$COLUMN_GOAL_ID = ?",
            arrayOf(goalId.toString()),
            null,
            null,
            null
        )

        return if (cursor != null && cursor.moveToFirst()) {
            val goal = Goal(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GOAL_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GOAL_USER_ID)),
                type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_TYPE)),
                target = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GOAL_TARGET)),
                current = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GOAL_CURRENT)),
                deadline = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_DEADLINE)),
                achieved = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GOAL_ACHIEVED)) == 1
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
        values.put(COLUMN_GOAL_CURRENT, current)

        val cursor = db.query(
            TABLE_GOALS,
            arrayOf(COLUMN_GOAL_TARGET),
            "$COLUMN_GOAL_ID = ?",
            arrayOf(goalId.toString()),
            null, null, null
        )

        if (cursor != null && cursor.moveToFirst()) {
            val target = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GOAL_TARGET))
            if (current >= target) {
                values.put(COLUMN_GOAL_ACHIEVED, 1)
            }
            cursor.close()
        }

        db.update(
            TABLE_GOALS,
            values,
            "$COLUMN_GOAL_ID = ?",
            arrayOf(goalId.toString())
        )
        db.close()
    }

    fun updateGoal(goal: Goal): Int {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COLUMN_GOAL_TYPE, goal.type)
        values.put(COLUMN_GOAL_TARGET, goal.target)
        values.put(COLUMN_GOAL_CURRENT, goal.current)
        values.put(COLUMN_GOAL_DEADLINE, goal.deadline)
        values.put(COLUMN_GOAL_ACHIEVED, if (goal.achieved) 1 else 0)

        val rowsAffected = db.update(
            TABLE_GOALS,
            values,
            "$COLUMN_GOAL_ID = ?",
            arrayOf(goal.id.toString())
        )
        db.close()
        return rowsAffected
    }

    fun deleteGoal(goalId: Int): Int {
        val db = this.writableDatabase
        val rowsAffected = db.delete(
            TABLE_GOALS,
            "$COLUMN_GOAL_ID = ?",
            arrayOf(goalId.toString())
        )
        db.close()
        return rowsAffected
    }

    fun getTotalWorkoutsCount(userId: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_WORKOUTS WHERE $COLUMN_WORKOUT_USER_ID = ?",
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
            "SELECT SUM($COLUMN_DURATION) FROM $TABLE_WORKOUTS WHERE $COLUMN_WORKOUT_USER_ID = ?",
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
            "SELECT SUM($COLUMN_CALORIES) FROM $TABLE_WORKOUTS WHERE $COLUMN_WORKOUT_USER_ID = ?",
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