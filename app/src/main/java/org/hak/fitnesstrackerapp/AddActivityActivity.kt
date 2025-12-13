package org.hak.fitnesstrackerapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.hak.fitnesstrackerapp.databinding.ActivityAddActivityBinding
import java.util.*

class AddActivityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddActivityBinding
    private lateinit var viewModel: ActivityViewModel

    private val activityTypes = listOf(
        "running", "cycling", "weightlifting", "swimming", "yoga", "walking", "other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel without Hilt
        viewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)

        setupToolbar()
        initViews()
        setupObservers()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Activity"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initViews() {
        // Setup activity type spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            activityTypes.map { it.replaceFirstChar { char -> char.uppercaseChar() } }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerActivityType.adapter = adapter

        // Setup date picker
        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        // Setup time picker
        binding.etTime.setOnClickListener {
            showTimePicker()
        }

        // Setup save button
        binding.btnSave.setOnClickListener {
            saveActivity()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
                binding.etDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                val selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                binding.etTime.setText(selectedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveActivity() {
        val activityType = binding.spinnerActivityType.selectedItem.toString().lowercase()
        val durationText = binding.etDuration.text.toString().trim()
        val distanceText = binding.etDistance.text.toString().trim()
        val caloriesText = binding.etCalories.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        // Validation
        if (durationText.isEmpty()) {
            binding.etDuration.error = "Duration is required"
            return
        }

        val duration = durationText.toIntOrNull()
        if (duration == null || duration <= 0) {
            binding.etDuration.error = "Invalid duration"
            return
        }

        val distance = if (distanceText.isNotEmpty()) distanceText.toDoubleOrNull() else null
        val calories = if (caloriesText.isNotEmpty()) caloriesText.toIntOrNull() else null

        // Comment out for now
        // viewModel.addActivity(
        //     activityType = activityType,
        //     duration = duration,
        //     distance = distance,
        //     calories = calories,
        //     notes = if (notes.isNotEmpty()) notes else null
        // )

        // For testing, just show success
        Toast.makeText(this, "Activity saved successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun setupObservers() {
        // Comment out for now
        /*
        viewModel.addActivityState.observe(this, Observer { state ->
            when (state) {
                is AddActivityState.Loading -> {
                    showLoading(true)
                }
                is AddActivityState.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "Activity saved successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is AddActivityState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        */
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnSave.isEnabled = !show
    }
}