package org.hak.fitnesstrackerapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hak.fitnesstrackerapp.databinding.ActivityWeightliftingBinding
import org.hak.fitnesstrackerapp.network.ActivityRequest
import org.hak.fitnesstrackerapp.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.*

class WeightliftingActivity : BaseActivity() {
    private lateinit var binding: ActivityWeightliftingBinding
    private val apiService = RetrofitClient.instance
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun setupActivity() {
        binding = ActivityWeightliftingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupExerciseSpinner()
        setupClickListeners()
        setDefaultDateTime()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Weightlifting"
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupExerciseSpinner() {
        val exercises = arrayOf(
            "Bench Press", "Squat", "Deadlift", "Shoulder Press",
            "Bicep Curls", "Tricep Extensions", "Lat Pulldown", "Leg Press"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, exercises)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spExercise.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnSelectDateTime.setOnClickListener {
            showDateTimePicker()
        }

        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                saveWeightliftingActivity()
            }
        }
    }

    private fun setDefaultDateTime() {
        val dateTime = String.format(
            "%02d/%02d/%04d %02d:%02d",
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )
        binding.tvSelectedDateTime.text = dateTime
    }

    private fun showDateTimePicker() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)

                val timePicker = TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        setDefaultDateTime()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun validateInput(): Boolean {
        var isValid = true

        if (binding.etSets.text.toString().isEmpty()) {
            binding.etSets.error = "Sets is required"
            isValid = false
        }

        if (binding.etReps.text.toString().isEmpty()) {
            binding.etReps.error = "Reps is required"
            isValid = false
        }

        if (binding.etWeight.text.toString().isEmpty()) {
            binding.etWeight.error = "Weight is required"
            isValid = false
        }

        if (binding.etDuration.text.toString().isEmpty()) {
            binding.etDuration.error = "Duration is required"
            isValid = false
        }

        if (binding.etCalories.text.toString().isEmpty()) {
            binding.etCalories.error = "Calories is required"
            isValid = false
        }

        return isValid
    }

    private fun saveWeightliftingActivity() {
        val exerciseName = binding.spExercise.selectedItem.toString()
        val sets = binding.etSets.text.toString().toIntOrNull() ?: 0
        val reps = binding.etReps.text.toString().toIntOrNull() ?: 0
        val weight = binding.etWeight.text.toString().toDoubleOrNull() ?: 0.0
        val duration = binding.etDuration.text.toString().toIntOrNull() ?: 0
        val calories = binding.etCalories.text.toString().toIntOrNull() ?: 0
        val note = binding.etNote.text.toString()

        // Format date for API
        val formattedDate = dateFormat.format(calendar.time)

        // Calculate estimated calories if not provided
        val estimatedCalories = if (calories == 0) {
            (sets * reps * weight * 0.1).toInt()
        } else {
            calories
        }

        val activityRequest = ActivityRequest(
            user_id = preferencesManager.userId,
            type = "Weightlifting",
            duration = duration,
            distance = 0.0,
            calories = estimatedCalories,
            note = note,
            date = formattedDate,
            exercise_name = exerciseName,
            sets = sets,
            reps = reps,
            weight = weight
        )

        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Saving..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.addActivity(activityRequest)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.success == true) { // Fixed here
                            showToast("Weightlifting activity saved successfully!")
                            finish()
                        } else {
                            showToast(apiResponse?.message ?: "Failed to save activity") // Fixed here
                        }
                    } else {
                        showToast("Server error: ${response.code()}")
                    }
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Save Activity"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLongToast("Network error: ${e.message}")
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Save Activity"
                }
            }
        }
    }
}