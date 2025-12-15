package org.hak.fitnesstrackerapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hak.fitnesstrackerapp.databinding.ActivityAddActivityBinding
import org.hak.fitnesstrackerapp.network.ActivityRequest
import org.hak.fitnesstrackerapp.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.*

class AddActivityActivity : BaseActivity() {
    private lateinit var binding: ActivityAddActivityBinding
    private val apiService = RetrofitClient.instance
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private var isWeightlifting = false

    override fun setupActivity() {
        binding = ActivityAddActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupSpinner()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Activity"
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupSpinner() {
        val activityTypes = arrayOf("Running", "Walking", "Cycling", "Swimming", "Gym", "Weightlifting", "Yoga")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activityTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spActivityType.adapter = adapter

        // Listen for Weightlifting selection
        binding.spActivityType.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                isWeightlifting = (activityTypes[position] == "Weightlifting")
                if (isWeightlifting) {
                    // Navigate to weightlifting activity
                    startActivity(Intent(this@AddActivityActivity, WeightliftingActivity::class.java))
                    finish()
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectDateTime.setOnClickListener {
            showDateTimePicker()
        }

        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                saveActivity()
            }
        }
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

                        val dateTime = String.format(
                            "%02d/%02d/%04d %02d:%02d",
                            calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE)
                        )
                        binding.tvSelectedDateTime.text = dateTime
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

        if (binding.etDuration.text.toString().isEmpty()) {
            binding.etDuration.error = "Duration is required"
            isValid = false
        }

        if (binding.etDistance.text.toString().isEmpty()) {
            binding.etDistance.error = "Distance is required"
            isValid = false
        }

        if (binding.etCalories.text.toString().isEmpty()) {
            binding.etCalories.error = "Calories is required"
            isValid = false
        }

        return isValid
    }

    private fun saveActivity() {
        val type = binding.spActivityType.selectedItem.toString()
        val duration = binding.etDuration.text.toString().toIntOrNull() ?: 0
        val distance = binding.etDistance.text.toString().toDoubleOrNull() ?: 0.0
        val calories = binding.etCalories.text.toString().toIntOrNull() ?: 0
        val note = binding.etNote.text.toString()

        // Format date for API
        val formattedDate = dateFormat.format(calendar.time)

        val activityRequest = ActivityRequest(
            user_id = preferencesManager.userId,
            type = type,
            duration = duration,
            distance = distance,
            calories = calories,
            note = note,
            date = formattedDate
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
                            showToast("Activity saved successfully!")
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