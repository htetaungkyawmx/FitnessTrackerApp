package org.hak.fitnesstrackerapp.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hdodenhof.circleimageview.CircleImageView
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.adapters.WorkoutAdapter
import org.hak.fitnesstrackerapp.database.SQLiteHelper
import org.hak.fitnesstrackerapp.model.Workout
import org.hak.fitnesstrackerapp.model.WorkoutRequest
import org.hak.fitnesstrackerapp.network.RetrofitClient
import org.hak.fitnesstrackerapp.utils.SharedPrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var tvWelcome: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvActiveEnergy: TextView
    private lateinit var tvSeeAll: TextView
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var emptyStateView: View
    private lateinit var btnAddFirst: androidx.cardview.widget.CardView
    private lateinit var ivProfile: CircleImageView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fabAddWorkout: FloatingActionButton
    private lateinit var workoutAdapter: WorkoutAdapter
    private lateinit var dbHelper: SQLiteHelper
    private var showingAllWorkouts = false
    private val TAG = "MainActivity"
    private var currentUserId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupBottomNavigation()
        setupRecyclerView()
        loadData()
        setupClickListeners()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvUserName = findViewById(R.id.tvUserName)
        tvDuration = findViewById(R.id.tvDuration)
        tvCalories = findViewById(R.id.tvCalories)
        tvActiveEnergy = findViewById(R.id.tvActiveEnergy)
        tvSeeAll = findViewById(R.id.tvSeeAll)
        recyclerView = findViewById(R.id.recyclerViewWorkouts)
        emptyStateView = findViewById(R.id.emptyStateView)
        btnAddFirst = findViewById(R.id.btnAddFirst)
        ivProfile = findViewById(R.id.ivProfile)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        fabAddWorkout = findViewById(R.id.fabAddWorkout)

        dbHelper = SQLiteHelper(this)

        val user = SharedPrefManager.getInstance(this).user
        if (user != null) {
            currentUserId = user.id
        } else {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_workouts -> {
                    val intent = Intent(this, WorkoutLogActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }
                R.id.nav_goals -> {
                    val intent = Intent(this, GoalsActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        workoutAdapter = WorkoutAdapter(emptyList()) { workout ->
            try {
                val intent = Intent(this, WorkoutDetailActivity::class.java)
                intent.putExtra("WORKOUT_ID", workout.id)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            } catch (e: Exception) {
                Toast.makeText(this, "Error opening workout details", Toast.LENGTH_SHORT).show()
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = workoutAdapter
    }

    private fun loadData() {
        val user = SharedPrefManager.getInstance(this).user
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            navigateToLogin()
            return
        }

        tvWelcome.text = "Welcome Back,"
        tvUserName.text = user.name

        loadTodayStats()
        loadRecentWorkouts()
    }

    private fun loadTodayStats() {
        try {
            if (currentUserId == 0) return

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val duration = dbHelper.getDailyDuration(currentUserId, today)
            val calories = dbHelper.getWeeklyCalories(currentUserId, today, today)
            val activeEnergy = if (calories > 0) (calories * 0.85).toInt() else 0

            tvDuration.text = duration.toString()
            tvCalories.text = calories.toString()
            tvActiveEnergy.text = activeEnergy.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading stats: ${e.message}")
            tvDuration.text = "0"
            tvCalories.text = "0"
            tvActiveEnergy.text = "0"
        }
    }

    private fun loadRecentWorkouts() {
        try {
            if (currentUserId == 0) return

            val workouts = dbHelper.getAllWorkouts(currentUserId)

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
        } catch (e: Exception) {
            Log.e(TAG, "Error loading workouts: ${e.message}")
            showEmptyState()
        }
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyStateView.visibility = View.VISIBLE
    }

    private fun showWorkoutList() {
        recyclerView.visibility = View.VISIBLE
        emptyStateView.visibility = View.GONE
    }

    private fun setupClickListeners() {
        ivProfile.setOnClickListener {
            try {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            } catch (e: Exception) {
                Toast.makeText(this, "Profile screen not available", Toast.LENGTH_SHORT).show()
            }
        }

        fabAddWorkout.setOnClickListener {
            showAddWorkoutDialog()
        }

        btnAddFirst.setOnClickListener {
            showAddWorkoutDialog()
        }

        tvSeeAll.setOnClickListener {
            if (currentUserId == 0) return@setOnClickListener

            val workouts = dbHelper.getAllWorkouts(currentUserId)
            if (workouts.isNotEmpty()) {
                showingAllWorkouts = !showingAllWorkouts
                loadRecentWorkouts()
            } else {
                Toast.makeText(this, "No workouts available", Toast.LENGTH_SHORT).show()
            }
        }

        setupCategoryClickListeners()
    }

    private fun setupCategoryClickListeners() {
        try {
            findViewById<androidx.cardview.widget.CardView>(R.id.card_running).setOnClickListener {
                handleWorkoutCategoryClick("Running", R.raw.running_animation, true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Running card not found: ${e.message}")
        }

        try {
            findViewById<androidx.cardview.widget.CardView>(R.id.card_cycling).setOnClickListener {
                handleWorkoutCategoryClick("Cycling", R.raw.cycling_animation, true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cycling card not found: ${e.message}")
        }

        try {
            findViewById<androidx.cardview.widget.CardView>(R.id.card_swimming).setOnClickListener {
                handleWorkoutCategoryClick("Swimming", R.raw.swimming_animation, false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Swimming card not found: ${e.message}")
        }

        try {
            findViewById<androidx.cardview.widget.CardView>(R.id.card_yoga).setOnClickListener {
                handleWorkoutCategoryClick("Yoga", R.raw.yoga_animation, false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Yoga card not found: ${e.message}")
        }

        try {
            findViewById<androidx.cardview.widget.CardView>(R.id.card_strength).setOnClickListener {
                handleWorkoutCategoryClick("Strength", R.raw.strength_animation, false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Strength card not found: ${e.message}")
        }

        try {
            findViewById<androidx.cardview.widget.CardView>(R.id.card_hiit).setOnClickListener {
                handleWorkoutCategoryClick("HIIT", R.raw.hiit_animation, false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "HIIT card not found: ${e.message}")
        }
    }

    private fun handleWorkoutCategoryClick(workoutType: String, animationRes: Int?, needsGPS: Boolean) {
        if (currentUserId == 0) return
        val actualWorkoutType = if (workoutType == "Strength") "Weightlifting" else workoutType
        val actualAnimationRes = if (workoutType == "Strength") R.raw.strength_animation else animationRes

        val hasWorkouts = dbHelper.hasWorkoutsByType(currentUserId, actualWorkoutType)

        if (!hasWorkouts) {
          showNoDataDialog(actualWorkoutType, actualAnimationRes, needsGPS)
        } else {

            if (needsGPS && (actualWorkoutType == "Running" || actualWorkoutType == "Cycling")) {
               startOSMTrackingActivity(actualWorkoutType, actualAnimationRes)
            } else {
               startAnimationActivity(actualWorkoutType, actualAnimationRes)
            }
        }
    }

    private fun startOSMTrackingActivity(workoutType: String, animationRes: Int?) {
        val intent = Intent(this, OSMTrackingActivity::class.java)
        intent.putExtra("WORKOUT_TYPE", workoutType)
        if (animationRes != null) {
            intent.putExtra("ANIMATION_RES", animationRes)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showNoDataDialog(workoutType: String, animationRes: Int?, needsGPS: Boolean) {
        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("No $workoutType Data Found")
            .setMessage("You don't have any $workoutType workouts yet. Please add your first workout data.")
            .setPositiveButton("Add Now") { dialog, which ->
                showAddWorkoutDialogWithType(workoutType)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startAnimationActivity(workoutType: String, animationRes: Int?) {
        val intent = Intent(this, AnimationActivity::class.java)
        intent.putExtra("WORKOUT_TYPE", workoutType)
        if (animationRes != null) {
            intent.putExtra("ANIMATION_RES", animationRes)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun startGPSTrackingActivity(workoutType: String, animationRes: Int?) {
        val intent = Intent(this, GPSTrackingActivity::class.java)
        intent.putExtra("WORKOUT_TYPE", workoutType)
        if (animationRes != null) {
            intent.putExtra("ANIMATION_RES", animationRes)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showAddWorkoutDialog() {
        showAddWorkoutDialogWithType(null)
    }

    private fun showAddWorkoutDialogWithType(preSelectedType: String?) {
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

        preSelectedType?.let { type ->
            val position = workoutTypes.indexOfFirst { it.equals(type, ignoreCase = true) }
            if (position >= 0) {
                spWorkoutType.setSelection(position)
            }
        }

        val dialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
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
        if (currentUserId == 0) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

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

        val duration = durationStr.toIntOrNull() ?: 0
        val distance = distanceStr.toDoubleOrNull()
        val calories = caloriesStr.toIntOrNull() ?: 0
        val timestamp = System.currentTimeMillis()

        val workout = Workout(
            userId = currentUserId,
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
            saveToServer(savedWorkout, currentUserId)

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
        call.enqueue(object : Callback<org.hak.fitnesstrackerapp.model.ApiResponse> {
            override fun onResponse(
                call: Call<org.hak.fitnesstrackerapp.model.ApiResponse>,
                response: Response<org.hak.fitnesstrackerapp.model.ApiResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
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

            override fun onFailure(call: Call<org.hak.fitnesstrackerapp.model.ApiResponse>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}")
            }
        })
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        val user = SharedPrefManager.getInstance(this).user
        currentUserId = user?.id ?: 0

        if (currentUserId == 0) {
            navigateToLogin()
            return
        }

        loadTodayStats()
        loadRecentWorkouts()

        bottomNavigation.selectedItemId = R.id.nav_home
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.closeDB()
    }
}