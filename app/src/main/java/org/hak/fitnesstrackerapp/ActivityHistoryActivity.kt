package org.hak.fitnesstrackerapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.hak.fitnesstrackerapp.databinding.ActivityHistoryBinding
import org.hak.fitnesstrackerapp.databinding.DialogFilterBinding
import org.hak.fitnesstrackerapp.network.models.Activity
import org.hak.fitnesstrackerapp.network.models.Statistics
import org.hak.fitnesstrackerapp.viewmodel.ActivityViewModel
import java.util.*

class ActivityHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: ActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel without Hilt
        viewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)

        setupToolbar()
        initViews()
        setupObservers()
        loadActivities()
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
        // For now, create a simple adapter or comment out
        // adapter = ActivityHistoryAdapter { activity ->
        //     showDeleteConfirmationDialog(activity.id)
        // }

        binding.rvActivities.layoutManager = LinearLayoutManager(this)
        // binding.rvActivities.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            loadActivities()
        }

        binding.fabAddActivity.setOnClickListener {
            // Navigate to add activity
        }
    }

    private fun setupObservers() {
        // Comment out for now
        /*
        viewModel.activitiesState.observe(this, Observer { state ->
            binding.swipeRefresh.isRefreshing = false

            when (state) {
                is ActivitiesState.Loading -> {
                    showLoading(true)
                }
                is ActivitiesState.Success -> {
                    showLoading(false)
                    updateActivitiesList(state.response.activities)
                    updateStatistics(state.response.statistics)
                }
                is ActivitiesState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        */
    }

    private fun loadActivities() {
        // viewModel.loadActivities()
    }

    private fun updateActivitiesList(activities: List<Activity>) {
        // adapter.submitList(activities)

        if (activities.isEmpty()) {
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            binding.rvActivities.visibility = android.view.View.GONE
        } else {
            binding.tvEmpty.visibility = android.view.View.GONE
            binding.rvActivities.visibility = android.view.View.VISIBLE
        }
    }

    private fun updateStatistics(statistics: Statistics) {
        binding.tvTotalActivities.text = "Total Activities: ${statistics.total_activities}"
        binding.tvTotalMinutes.text = "Total Minutes: ${statistics.total_minutes}"
        binding.tvTotalCalories.text = "Total Calories: ${statistics.total_calories}"
    }

    private fun showFilterDialog() {
        val dialogBinding = DialogFilterBinding.inflate(layoutInflater)

        val activityTypes = listOf(
            "All", "Running", "Cycling", "Weightlifting", "Swimming", "Yoga", "Walking", "Other"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activityTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerType.adapter = adapter

        dialogBinding.etDateFrom.setOnClickListener {
            showDatePicker(dialogBinding.etDateFrom)
        }

        dialogBinding.etDateTo.setOnClickListener {
            showDatePicker(dialogBinding.etDateTo)
        }

        AlertDialog.Builder(this)
            .setTitle("Filter Activities")
            .setView(dialogBinding.root)
            .setPositiveButton("Apply") { _, _ ->
                val type = if (dialogBinding.spinnerType.selectedItemPosition > 0) {
                    dialogBinding.spinnerType.selectedItem.toString().lowercase()
                } else null

                // currentType = type
                // currentDateFrom = dialogBinding.etDateFrom.text.toString().takeIf { it.isNotEmpty() }
                // currentDateTo = dialogBinding.etDateTo.text.toString().takeIf { it.isNotEmpty() }

                // applyFilters()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker(editText: android.widget.EditText) {
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
        // viewModel.searchActivities(currentType, currentDateFrom, currentDateTo)
    }

    private fun clearFilters() {
        // currentType = null
        // currentDateFrom = null
        // currentDateTo = null
        loadActivities()
    }

    private fun showDeleteConfirmationDialog(activityId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Activity")
            .setMessage("Are you sure you want to delete this activity?")
            .setPositiveButton("Delete") { _, _ ->
                // viewModel.deleteActivity(activityId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
}
