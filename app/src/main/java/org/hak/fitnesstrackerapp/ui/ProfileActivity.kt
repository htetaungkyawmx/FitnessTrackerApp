package org.azm.fitness_app.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.azm.fitness_app.R
import org.azm.fitness_app.database.SQLiteHelper
import org.azm.fitness_app.model.WorkoutRequest
import org.azm.fitness_app.network.RetrofitClient
import org.azm.fitness_app.utils.SharedPrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class ProfileActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var tvProfileInitial: TextView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvHeight: TextView
    private lateinit var tvBMI: TextView
    private lateinit var tvTotalWorkouts: TextView
    private lateinit var tvTotalDuration: TextView
    private lateinit var tvTotalCalories: TextView
    private lateinit var btnSync: Button
    private lateinit var btnLogout: Button
    private lateinit var btnEdit: Button
    private lateinit var btnGoals: Button
    private lateinit var dbHelper: SQLiteHelper
    private val TAG = "ProfileActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setupToolbar()
        initDatabase()
        initializeViews()
        setupClickListeners()
        loadUserProfile()
        loadStatistics()
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Profile"
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun initDatabase() {
        dbHelper = SQLiteHelper(this)
    }

    private fun initializeViews() {
        tvProfileInitial = findViewById(R.id.tvProfileInitial)
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        tvAge = findViewById(R.id.tvAge)
        tvWeight = findViewById(R.id.tvWeight)
        tvHeight = findViewById(R.id.tvHeight)
        tvBMI = findViewById(R.id.tvBMI)
        tvTotalWorkouts = findViewById(R.id.tvTotalWorkouts)
        tvTotalDuration = findViewById(R.id.tvTotalDuration)
        tvTotalCalories = findViewById(R.id.tvTotalCalories)
        btnSync = findViewById(R.id.btnSync)
        btnLogout = findViewById(R.id.btnLogout)
        btnEdit = findViewById(R.id.btnEdit)
        btnGoals = findViewById(R.id.btnGoals)
    }

    private fun setupClickListeners() {
        btnSync.setOnClickListener {
            syncWithServer()
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        btnEdit.setOnClickListener {
            navigateToEditProfile()
        }

        btnGoals.setOnClickListener {
            navigateToGoals()
        }
    }

    private fun loadUserProfile() {
        val user = SharedPrefManager.getInstance(this).user

        if (user != null && user.id != 0 && user.name.isNotEmpty()) {
            // Set profile initial (first letter of name)
            val initial = if (user.name.isNotEmpty()) user.name[0].uppercaseChar().toString() else "U"
            tvProfileInitial.text = initial

            // Set random color for profile icon
            setRandomProfileColor(initial)

            // Set name and email
            tvName.text = user.name
            tvEmail.text = user.email

            // Set age (if available)
            if (user.age > 0) {
                tvAge.text = "${user.age} years"
            } else {
                tvAge.text = "Not set"
            }

            // Set weight (if available)
            if (user.weight > 0) {
                tvWeight.text = "${String.format("%.1f", user.weight)} kg"
            } else {
                tvWeight.text = "Not set"
            }

            // Set height (if available)
            if (user.height > 0) {
                tvHeight.text = "${String.format("%.1f", user.height)} cm"
            } else {
                tvHeight.text = "Not set"
            }

            // Calculate and set BMI (if height is available)
            calculateAndSetBMI(user)
        } else {
            // If no valid user, navigate to LoginActivity
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    private fun calculateAndSetBMI(user: org.azm.fitness_app.model.User) {
        if (user.height > 0 && user.weight > 0) {
            val heightInMeters = user.height / 100.0
            val bmi = user.weight / (heightInMeters * heightInMeters)
            val df = DecimalFormat("#.##")

            val bmiCategory = when {
                bmi < 18.5 -> " (Underweight)"
                bmi < 25 -> " (Normal)"
                bmi < 30 -> " (Overweight)"
                else -> " (Obese)"
            }
            tvBMI.text = "${df.format(bmi)}$bmiCategory"

            // Set BMI text color based on category
            val bmiColor = when {
                bmi < 18.5 -> Color.parseColor("#FFA726") // Orange
                bmi < 25 -> Color.parseColor("#4CAF50")   // Green
                bmi < 30 -> Color.parseColor("#FF9800")   // Orange
                else -> Color.parseColor("#F44336")       // Red
            }
            tvBMI.setTextColor(bmiColor)
        } else {
            tvBMI.text = "N/A"
            tvBMI.setTextColor(Color.parseColor("#757575")) // Gray color
        }
    }

    private fun setRandomProfileColor(initial: String) {
        // Generate a consistent color based on the initial
        val colors = arrayOf(
            "#FF6B6B", "#4ECDC4", "#FFD166", "#06D6A0",
            "#118AB2", "#EF476F", "#7209B7", "#3A86FF"
        )

        val charCode = initial[0].code
        val colorIndex = charCode % colors.size
        tvProfileInitial.setBackgroundColor(Color.parseColor(colors[colorIndex]))
    }

    private fun loadStatistics() {
        try {
            val workouts = dbHelper.getAllWorkouts()
            val totalWorkouts = workouts.size
            val totalDuration = workouts.sumOf { it.duration }
            val totalCalories = workouts.sumOf { it.calories }

            tvTotalWorkouts.text = totalWorkouts.toString()
            tvTotalDuration.text = totalDuration.toString()
            tvTotalCalories.text = totalCalories.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading statistics: ${e.message}")
            Toast.makeText(this, "Error loading statistics", Toast.LENGTH_SHORT).show()
        }
    }

    private fun syncWithServer() {
        val userId = SharedPrefManager.getInstance(this).user?.id ?: return

        val unsyncedWorkouts = dbHelper.getUnsyncedWorkouts()

        if (unsyncedWorkouts.isEmpty()) {
            Toast.makeText(this, "✓ All data is already synced", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Syncing ${unsyncedWorkouts.size} workouts...", Toast.LENGTH_SHORT).show()

        // Prepare sync data
        val syncData = HashMap<String, Any>()
        syncData["userId"] = userId
        syncData["workouts"] = unsyncedWorkouts.map { workout ->
            val workoutMap = HashMap<String, Any>()
            workoutMap["type"] = workout.type
            workoutMap["duration"] = workout.duration
            workout.distance?.let { workoutMap["distance"] = it }
            workoutMap["calories"] = workout.calories
            workoutMap["notes"] = workout.notes
            workoutMap["date"] = workout.date
            workoutMap["timestamp"] = workout.timestamp
            workoutMap
        }

        Log.d(TAG, "Syncing data: ${syncData}")

        val call = RetrofitClient.instance.syncWorkouts(syncData)
        call.enqueue(object : Callback<org.azm.fitness_app.model.ApiResponse> {
            override fun onResponse(
                call: Call<org.azm.fitness_app.model.ApiResponse>,
                response: Response<org.azm.fitness_app.model.ApiResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            // Mark all as synced
                            unsyncedWorkouts.forEach { workout ->
                                dbHelper.updateWorkoutSyncStatus(workout.id, true)
                            }

                            loadStatistics() // Refresh stats
                            Toast.makeText(
                                this@ProfileActivity,
                                "✓ Successfully synced ${unsyncedWorkouts.size} workouts",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@ProfileActivity,
                                "Sync failed: ${apiResponse.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Server error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<org.azm.fitness_app.model.ApiResponse>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}")
                Toast.makeText(
                    this@ProfileActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun navigateToEditProfile() {
        Toast.makeText(this, "Edit Profile feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToGoals() {
        try {
            val intent = Intent(this, GoalsActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Goals feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes, Logout") { dialog, which ->
                logoutUser()
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(true)
            .show()
    }

    private fun logoutUser() {
        try {
            // Clear user data from SharedPreferences
            SharedPrefManager.getInstance(this).clear()

            // Navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Logout failed: ${e.message}")
            Toast.makeText(this, "Logout failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this activity
        loadUserProfile()
        loadStatistics()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            dbHelper.closeDB()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing database: ${e.message}")
        }
    }
}