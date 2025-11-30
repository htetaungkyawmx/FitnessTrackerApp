package org.hak.fitnesstrackerapp.ui.activities

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.database.AppDatabase
import org.hak.fitnesstrackerapp.databinding.ActivityProfileBinding
import org.hak.fitnesstrackerapp.utils.PreferenceHelper
import org.hak.fitnesstrackerapp.utils.showToast

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var database: AppDatabase
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = org.hak.fitnesstrackerapp.FitnessTrackerApp.instance.database
        preferenceHelper = PreferenceHelper(this)

        setupToolbar()
        loadUserData()
        setupClickListeners()
        loadStatistics()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val user = preferenceHelper.getUser()
            user?.let {
                binding.usernameEditText.setText(it.username)
                binding.emailEditText.setText(it.email)
                binding.heightEditText.setText(it.height?.toString() ?: "")
                binding.weightEditText.setText(it.weight?.toString() ?: "")
                binding.ageEditText.setText(it.age?.toString() ?: "")

                // Update header
                binding.usernameTextView.text = it.username
                binding.emailTextView.text = it.email
            }
        }
    }

    private fun setupClickListeners() {
        binding.saveButton.setOnClickListener {
            updateProfile()
        }

        binding.heightTextInputLayout.setEndIconOnClickListener {
            showHeightDialog()
        }

        binding.weightTextInputLayout.setEndIconOnClickListener {
            showWeightDialog()
        }

        binding.statsCard.setOnClickListener {
            showDetailedStatistics()
        }
    }

    private fun updateProfile() {
        val height = binding.heightEditText.text.toString().toDoubleOrNull()
        val weight = binding.weightEditText.text.toString().toDoubleOrNull()
        val age = binding.ageEditText.text.toString().toIntOrNull()

        // Validate input
        if (height != null && (height < 100 || height > 250)) {
            binding.heightTextInputLayout.error = "Please enter a valid height (100-250 cm)"
            return
        }

        if (weight != null && (weight < 30 || weight > 300)) {
            binding.weightTextInputLayout.error = "Please enter a valid weight (30-300 kg)"
            return
        }

        if (age != null && (age < 10 || age > 100)) {
            binding.ageTextInputLayout.error = "Please enter a valid age (10-100)"
            return
        }

        lifecycleScope.launch {
            try {
                val currentUser = preferenceHelper.getUser()
                currentUser?.let { user ->
                    val updatedUser = user.copy(
                        height = height,
                        weight = weight,
                        age = age
                    )
                    database.userDao().updateUser(updatedUser)
                    preferenceHelper.saveUserSession(updatedUser)
                    showToast("Profile updated successfully")

                    // Clear errors
                    binding.heightTextInputLayout.error = null
                    binding.weightTextInputLayout.error = null
                    binding.ageTextInputLayout.error = null
                }
            } catch (e: Exception) {
                showToast("Failed to update profile: ${e.message}")
            }
        }
    }

    private fun showHeightDialog() {
        val heights = (140..220).map { it.toString() + " cm" }
        AlertDialog.Builder(this)
            .setTitle("Select Height")
            .setItems(heights.toTypedArray()) { dialog, which ->
                val height = heights[which].replace(" cm", "").toInt()
                binding.heightEditText.setText(height.toString())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showWeightDialog() {
        val weights = (40..150).map { it.toString() + " kg" }
        AlertDialog.Builder(this)
            .setTitle("Select Weight")
            .setItems(weights.toTypedArray()) { dialog, which ->
                val weight = weights[which].replace(" kg", "").toInt()
                binding.weightEditText.setText(weight.toString())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadStatistics() {
        lifecycleScope.launch {
            val userId = preferenceHelper.getUserId()

            val totalWorkouts = database.workoutDao().getTotalWorkouts(userId)
            val totalCalories = database.workoutDao().getTotalCalories(userId)
            val completedGoals = database.goalDao().getCompletedGoalsCount(userId)

            binding.totalWorkoutsStat.text = totalWorkouts.toString()
            binding.totalCaloriesStat.text = String.format("%.0f", totalCalories)
            binding.completedGoalsStat.text = completedGoals.toString()
        }
    }

    private fun showDetailedStatistics() {
        lifecycleScope.launch {
            val userId = preferenceHelper.getUserId()

            val totalWorkouts = database.workoutDao().getTotalWorkouts(userId)
            val totalCalories = database.workoutDao().getTotalCalories(userId)
            val totalDuration = database.workoutDao().getTotalDuration(userId)
            val runningDistance = database.workoutDao().getTotalRunningDistance(userId)
            val cyclingDistance = database.workoutDao().getTotalCyclingDistance(userId)
            val completedGoals = database.goalDao().getCompletedGoalsCount(userId)
            val totalGoals = database.goalDao().getTotalGoalsCount(userId)

            val message = """
                📊 Detailed Statistics:
                
                🏋️ Total Workouts: $totalWorkouts
                🔥 Total Calories: ${String.format("%.0f", totalCalories)} cal
                ⏱️ Total Duration: $totalDuration min
                
                🏃 Running Distance: ${String.format("%.1f", runningDistance)} km
                🚴 Cycling Distance: ${String.format("%.1f", cyclingDistance)} km
                
                🎯 Goals: $completedGoals/$totalGoals completed
                
                Keep up the great work! 💪
            """.trimIndent()

            AlertDialog.Builder(this@ProfileActivity)
                .setTitle("Your Fitness Statistics")
                .setMessage(message)
                .setPositiveButton("Awesome!") { dialog, which -> }
                .show()
        }
    }

    private fun calculateBMI(weight: Double?, height: Double?): Double? {
        return if (weight != null && height != null && height > 0) {
            val heightInMeters = height / 100
            weight / (heightInMeters * heightInMeters)
        } else {
            null
        }
    }

    private fun getBMICategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Normal weight"
            bmi < 30 -> "Overweight"
            else -> "Obese"
        }
    }
}
