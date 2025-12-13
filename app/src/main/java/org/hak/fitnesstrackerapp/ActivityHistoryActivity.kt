package org.hak.fitnesstrackerapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.hak.fitnesstrackerapp.adapter.ActivityAdapter
import org.hak.fitnesstrackerapp.databinding.ActivityHistoryBinding
import org.hak.fitnesstrackerapp.model.Activity
import org.hak.fitnesstrackerapp.model.ActivityType
import java.util.*

class ActivityHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: ActivityAdapter
    private var currentType: String? = null
    private var currentDateFrom: String? = null
    private var currentDateTo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        initViews()
        loadSampleActivities()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Activity History"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.history_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                showFilterDialog()
                true
            }
            R.id.action_clear_filter -> {
                clearFilters()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initViews() {
        adapter = ActivityAdapter { activity ->
            showDeleteConfirmationDialog(activity.id)
        }

        binding.rvActivities.layoutManager = LinearLayoutManager(this)
        binding.rvActivities.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            loadSampleActivities()
        }

        binding.fabAddActivity.setOnClickListener {
            Toast.makeText(this, "Add Activity feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSampleActivities() {
        // Create sample activities for testing
        val sampleActivities = listOf(
            Activity(
                id = 1,
                userId = 1,
                type = ActivityType.Running,
                durationMinutes = 30,
                distanceKm = 5.0,
                caloriesBurned = 300,
                notes = "Morning run in central park",
                createdAt = Date(),
                location = null
            ),
            Activity(
                id = 2,
                userId = 1,
                type = ActivityType.Weightlifting,
                durationMinutes = 60,
                distanceKm = null,
                caloriesBurned = 250,
                notes = "Chest and triceps workout",
                createdAt = Date(System.currentTimeMillis() - 86400000), // Yesterday
                location = null
            ),
            Activity(
                id = 3,
                userId = 1,
                type = ActivityType.Cycling,
                durationMinutes = 45,
                distanceKm = 15.0,
                caloriesBurned = 400,
                notes = null,
                createdAt = Date(System.currentTimeMillis() - 172800000), // 2 days ago
                location = null
            )
        )

        adapter.submitList(sampleActivities)
        updateStatistics(sampleActivities)
        binding.swipeRefresh.isRefreshing = false

        if (sampleActivities.isEmpty()) {
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            binding.rvActivities.visibility = android.view.View.GONE
        } else {
            binding.tvEmpty.visibility = android.view.View.GONE
            binding.rvActivities.visibility = android.view.View.VISIBLE
        }
    }

    private fun updateStatistics(activities: List<Activity>) {
        val totalActivities = activities.size
        val totalMinutes = activities.sumOf { it.durationMinutes }
        val totalCalories = activities.sumOf { it.calories }
        val totalDistance = activities.sumOf { it.distanceKm ?: 0.0 }

        binding.tvTotalActivities.text = "Total Activities: $totalActivities"
        binding.tvTotalMinutes.text = "Total Minutes: $totalMinutes"
        binding.tvTotalCalories.text = "Total Calories: $totalCalories"
        binding.tvTotalDistance.text = String.format("Total Distance: %.2f km", totalDistance)
    }

    private fun showFilterDialog() {
        // Create custom dialog layout programmatically
        val dialogView = layoutInflater.inflate(R.layout.dialog_filter, null)
        val spinnerType = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerType)
        val etDateFrom = dialogView.findViewById<EditText>(R.id.etDateFrom)
        val etDateTo = dialogView.findViewById<EditText>(R.id.etDateTo)

        val activityTypes = listOf(
            "All", "Running", "Cycling", "Weightlifting", "Swimming", "Yoga", "Walking", "Other"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activityTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        etDateFrom.setOnClickListener {
            showDatePicker(etDateFrom)
        }

        etDateTo.setOnClickListener {
            showDatePicker(etDateTo)
        }

        AlertDialog.Builder(this)
            .setTitle("Filter Activities")
            .setView(dialogView)
            .setPositiveButton("Apply") { _, _ ->
                val type = if (spinnerType.selectedItemPosition > 0) {
                    spinnerType.selectedItem.toString().lowercase()
                } else null

                currentType = type
                currentDateFrom = etDateFrom.text.toString().takeIf { it.isNotEmpty() }
                currentDateTo = etDateTo.text.toString().takeIf { it.isNotEmpty() }

                applyFilters()
                Toast.makeText(this, "Filters applied!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
                editText.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun applyFilters() {
        loadSampleActivities()
        Toast.makeText(this, "Filter: $currentType, From: $currentDateFrom, To: $currentDateTo", Toast.LENGTH_SHORT).show()
    }

    private fun clearFilters() {
        currentType = null
        currentDateFrom = null
        currentDateTo = null
        loadSampleActivities()
        Toast.makeText(this, "Filters cleared!", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmationDialog(activityId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Activity")
            .setMessage("Are you sure you want to delete this activity?")
            .setPositiveButton("Delete") { _, _ ->
                // Remove activity from list
                val newList = adapter.currentList.filter { it.id != activityId }
                adapter.submitList(newList)
                updateStatistics(newList)
                Toast.makeText(this, "Activity deleted!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}