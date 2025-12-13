package org.hak.fitnesstrackerapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hak.fitnesstrackerapp.databinding.ActivityAddActivityBinding
import org.hak.fitnesstrackerapp.network.ActivityRequest
import org.hak.fitnesstrackerapp.network.ApiResponse
import org.hak.fitnesstrackerapp.network.RetrofitClient
import org.hak.fitnesstrackerapp.utils.PreferencesManager
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class AddActivityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddActivityBinding
    private lateinit var preferencesManager: PreferencesManager
    private val apiService = RetrofitClient.instance  // ✅ Add this line
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

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
        val activityTypes = arrayOf("Running", "Walking", "Cycling", "Swimming", "Gym", "Yoga")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activityTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spActivityType.adapter = adapter
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
        if (binding.etDuration.text.toString().isEmpty()) {
            binding.etDuration.error = "Duration is required"
            return false
        }

        if (binding.etDistance.text.toString().isEmpty()) {
            binding.etDistance.error = "Distance is required"
            return false
        }

        if (binding.etCalories.text.toString().isEmpty()) {
            binding.etCalories.error = "Calories is required"
            return false
        }

        return true
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
                    handleSaveResponse(response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddActivityActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Save Activity"
                }
            }
        }
    }

    private fun handleSaveResponse(response: Response<ApiResponse>) {
        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse?.success == true) {
                Toast.makeText(this, "Activity saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, apiResponse?.message ?: "Failed to save activity", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
        }
        binding.btnSave.isEnabled = true
        binding.btnSave.text = "Save Activity"
    }
}
