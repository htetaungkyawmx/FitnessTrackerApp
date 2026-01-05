package org.azm.fitness_app.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.azm.fitness_app.R
import org.azm.fitness_app.database.SQLiteHelper
import org.azm.fitness_app.model.Workout
import org.azm.fitness_app.network.RetrofitClient
import org.azm.fitness_app.utils.SharedPrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WorkoutLogActivity : AppCompatActivity() {
    private lateinit var spinnerType: Spinner
    private lateinit var etDuration: EditText
    private lateinit var etDistance: EditText
    private lateinit var etCalories: EditText
    private lateinit var etNotes: EditText
    private lateinit var tvDate: TextView
    private lateinit var btnDate: Button
    private lateinit var btnSave: Button
    private lateinit var dbHelper: SQLiteHelper

    private var selectedDate = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_log)

        dbHelper = SQLiteHelper(this)

        spinnerType = findViewById(R.id.spinnerType)
        etDuration = findViewById(R.id.etDuration)
        etDistance = findViewById(R.id.etDistance)
        etCalories = findViewById(R.id.etCalories)
        etNotes = findViewById(R.id.etNotes)
        tvDate = findViewById(R.id.tvDate)
        btnDate = findViewById(R.id.btnDate)
        btnSave = findViewById(R.id.btnSave)

        // Setup workout type spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.workout_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerType.adapter = adapter
        }

        // Setup date picker
        updateDateDisplay()
        btnDate.setOnClickListener {
            showDatePicker()
        }

        btnSave.setOnClickListener {
            saveWorkout()
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate.set(year, month, day)
                updateDateDisplay()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        tvDate.text = dateFormat.format(selectedDate.time)
    }

    private fun saveWorkout() {
        val type = spinnerType.selectedItem.toString()
        val duration = etDuration.text.toString().toIntOrNull() ?: 0
        val distance = etDistance.text.toString().toDoubleOrNull() ?: 0.0
        val calories = etCalories.text.toString().toIntOrNull() ?: 0
        val notes = etNotes.text.toString()
        val date = tvDate.text.toString()
        val userId = SharedPrefManager.getInstance(this).user?.id ?: 0

        if (duration <= 0) {
            Toast.makeText(this, "Please enter duration", Toast.LENGTH_SHORT).show()
            return
        }

        if (type == "Running" || type == "Cycling") {
            if (distance <= 0) {
                Toast.makeText(this, "Please enter distance", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Save to local database
        val workout = Workout(
            userId = userId,
            type = type,
            duration = duration,
            distance = if (distance > 0) distance else null,
            calories = calories,
            notes = notes,
            date = date,
            synced = false
        )

        // Insert into SQLite
        val workoutId = dbHelper.insertWorkout(workout)

        if (workoutId != -1L) {
            // Update workout with ID
            val savedWorkout = workout.copy(id = workoutId.toInt())

            // Try to sync with server
            syncWithServer(savedWorkout, userId)

            Toast.makeText(this, "Workout saved!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to save workout", Toast.LENGTH_SHORT).show()
        }
    }

    private fun syncWithServer(workout: Workout, userId: Int) {
        val workoutRequest = org.azm.fitness_app.model.WorkoutRequest(
            userId = userId,
            type = workout.type,
            duration = workout.duration,
            distance = workout.distance,
            calories = workout.calories,
            notes = workout.notes,
            date = workout.date
        )

        val call = RetrofitClient.instance.addWorkout(workoutRequest)
        call.enqueue(object : Callback<org.azm.fitness_app.model.ApiResponse> {
            override fun onResponse(
                call: Call<org.azm.fitness_app.model.ApiResponse>,
                response: Response<org.azm.fitness_app.model.ApiResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    // Mark as synced
                    dbHelper.updateWorkoutSyncStatus(workout.id, true)
                }
            }

            override fun onFailure(call: Call<org.azm.fitness_app.model.ApiResponse>, t: Throwable) {
                // Keep as unsynced for later sync
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.closeDB()
    }
}
