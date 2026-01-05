package org.azm.fitness_app.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.azm.fitness_app.R
import org.azm.fitness_app.adapters.WorkoutAdapter
import org.azm.fitness_app.database.SQLiteHelper
import org.azm.fitness_app.model.Workout
import org.azm.fitness_app.model.WorkoutRequest
import org.azm.fitness_app.network.RetrofitClient
import org.azm.fitness_app.utils.SharedPrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var tvWelcome: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvTodayStats: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvSeeAll: TextView
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var emptyStateView: View
    private lateinit var btnAddFirst: Button
    private lateinit var ivProfile: ImageView
    private lateinit var workoutAdapter: WorkoutAdapter
    private lateinit var dbHelper: SQLiteHelper
    private var showingAllWorkouts = false
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupToolbar()
        setupRecyclerView()
        loadData()
        setupClickListeners()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvUserName = findViewById(R.id.tvUserName)
        tvTodayStats = findViewById(R.id.tvTodayStats)
        tvDuration = findViewById(R.id.tvDuration)
        tvCalories = findViewById(R.id.tvCalories)
        tvSeeAll = findViewById(R.id.tvSeeAll)
        recyclerView = findViewById(R.id.recyclerViewWorkouts)
        emptyStateView = findViewById(R.id.emptyStateView)
        btnAddFirst = findViewById(R.id.btnAddFirst)
        ivProfile = findViewById(R.id.ivProfile)

        dbHelper = SQLiteHelper(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
            setDisplayShowHomeEnabled(false)
        }
    }

    private fun setupRecyclerView() {
        workoutAdapter = WorkoutAdapter(emptyList()) { workout ->
            try {
                val intent = Intent(this, WorkoutDetailActivity::class.java)
                intent.putExtra("WORKOUT_ID", workout.id)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Error opening workout details", Toast.LENGTH_SHORT).show()
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = workoutAdapter
    }

    private fun loadData() {
        val user = SharedPrefManager.getInstance(this).user
        tvWelcome.text = "Welcome back,"
        tvUserName.text = user?.name ?: "User"

        loadTodayStats()
        loadRecentWorkouts()
    }

    private fun loadTodayStats() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val duration = dbHelper.getDailyDuration(today)
        val calories = dbHelper.getWeeklyCalories(today, today)

        tvTodayStats.text = "Today: ${duration} Min | ${calories} Kcal"
        tvDuration.text = duration.toString()
        tvCalories.text = calories.toString()
    }

    private fun loadRecentWorkouts() {
        val workouts = dbHelper.getAllWorkouts()

        if (workouts.isEmpty()) {
            showEmptyState()
        } else {
            showWorkoutList()
            if (showingAllWorkouts) {
                workoutAdapter.updateData(workouts)
                tvSeeAll.text = "Show Less"
            } else {
                workoutAdapter.updateData(workouts.take(5))
                tvSeeAll.text = "See All"
            }
        }
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyStateView.visibility = View.VISIBLE
        tvSeeAll.visibility = View.GONE
    }

    private fun showWorkoutList() {
        recyclerView.visibility = View.VISIBLE
        emptyStateView.visibility = View.GONE
        tvSeeAll.visibility = View.VISIBLE
    }

    private fun setupClickListeners() {
        ivProfile.setOnClickListener {
            try {
                startActivity(Intent(this, ProfileActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, "Profile screen not available", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<FloatingActionButton>(R.id.fabAddWorkout).setOnClickListener {
            showAddWorkoutDialog()
        }

        btnAddFirst.setOnClickListener {
            showAddWorkoutDialog()
        }

        tvSeeAll.setOnClickListener {
            val workouts = dbHelper.getAllWorkouts()
            if (workouts.isNotEmpty()) {
                showingAllWorkouts = !showingAllWorkouts
                loadRecentWorkouts()
            } else {
                Toast.makeText(this, "No workouts available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddWorkoutDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_workout, null)

        val spWorkoutType = dialogView.findViewById<Spinner>(R.id.spWorkoutType)
        val etDuration = dialogView.findViewById<EditText>(R.id.etDuration)
        val etDistance = dialogView.findViewById<EditText>(R.id.etDistance)
        val etCalories = dialogView.findViewById<EditText>(R.id.etCalories)
        val etNotes = dialogView.findViewById<EditText>(R.id.etNotes)
        val etDate = dialogView.findViewById<EditText>(R.id.etDate)

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        etDate.setText(today)

        etDate.setOnClickListener {
            showDatePickerDialog(etDate)
        }

        val workoutTypes = arrayOf("Running", "Cycling", "Swimming", "Walking", "Weightlifting", "Yoga", "HIIT", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, workoutTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spWorkoutType.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Workout")
            .setView(dialogView)
            .setPositiveButton("Save") { dialogInterface, which ->
                saveManualWorkout(
                    spWorkoutType.selectedItem.toString(),
                    etDuration.text.toString(),
                    etDistance.text.toString(),
                    etCalories.text.toString(),
                    etNotes.text.toString(),
                    etDate.text.toString()
                )
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun showDatePickerDialog(etDate: EditText) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                etDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun saveManualWorkout(
        type: String,
        durationStr: String,
        distanceStr: String,
        caloriesStr: String,
        notes: String,
        date: String
    ) {
        // Validation
        if (type.isEmpty()) {
            Toast.makeText(this, "Please select workout type", Toast.LENGTH_SHORT).show()
            return
        }

        if (durationStr.isEmpty()) {
            Toast.makeText(this, "Please enter duration", Toast.LENGTH_SHORT).show()
            return
        }

        if (caloriesStr.isEmpty()) {
            Toast.makeText(this, "Please enter calories", Toast.LENGTH_SHORT).show()
            return
        }

        if (date.isEmpty()) {
            Toast.makeText(this, "Please select date", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = SharedPrefManager.getInstance(this).user?.id ?: 1
        val duration = durationStr.toIntOrNull() ?: 0
        val distance = distanceStr.toDoubleOrNull()
        val calories = caloriesStr.toIntOrNull() ?: 0
        val timestamp = System.currentTimeMillis()

        // 1. Save to local SQLite database
        val workout = Workout(
            userId = userId,
            type = type,
            duration = duration,
            distance = distance,
            calories = calories,
            notes = notes,
            date = date,
            timestamp = timestamp,
            synced = false
        )

        val localId = dbHelper.insertWorkout(workout)

        if (localId > 0) {
            val savedWorkout = workout.copy(id = localId.toInt())

            // 2. Immediately try to save to server
            saveToServer(savedWorkout, userId)

            Toast.makeText(this, "$type workout added!", Toast.LENGTH_SHORT).show()
            loadTodayStats()
            loadRecentWorkouts()
        } else {
            Toast.makeText(this, "Failed to save workout locally", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToServer(workout: Workout, userId: Int) {
        val workoutRequest = WorkoutRequest(
            userId = userId,
            type = workout.type,
            duration = workout.duration,
            distance = workout.distance,
            calories = workout.calories,
            notes = workout.notes,
            date = workout.date,
            timestamp = workout.timestamp
        )

        val call = RetrofitClient.instance.addWorkout(workoutRequest)
        call.enqueue(object : Callback<org.azm.fitness_app.model.ApiResponse> {
            override fun onResponse(
                call: Call<org.azm.fitness_app.model.ApiResponse>,
                response: Response<org.azm.fitness_app.model.ApiResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            // Mark as synced in local database
                            dbHelper.updateWorkoutSyncStatus(workout.id, true)
                            Log.d(TAG, "Workout ${workout.id} synced to server successfully")
                        } else {
                            Log.w(TAG, "Server rejected workout: ${apiResponse.message}")
                        }
                    }
                } else {
                    Log.w(TAG, "Server error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<org.azm.fitness_app.model.ApiResponse>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}")
                // Keep as unsynced, will try again later
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadTodayStats()
        loadRecentWorkouts()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.closeDB()
    }
}