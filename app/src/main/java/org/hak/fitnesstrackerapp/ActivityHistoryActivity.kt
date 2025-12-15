package org.hak.fitnesstrackerapp

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hak.fitnesstrackerapp.adapters.ActivityAdapter
import org.hak.fitnesstrackerapp.databinding.ActivityActivityHistoryBinding
import org.hak.fitnesstrackerapp.network.RetrofitClient
import java.util.*

class ActivityHistoryActivity : BaseActivity() {
    private lateinit var binding: ActivityActivityHistoryBinding
    private lateinit var activityAdapter: ActivityAdapter
    private val apiService = RetrofitClient.instance
    private val allActivities = mutableListOf<org.hak.fitnesstrackerapp.models.FitnessActivity>()

    override fun setupActivity() {
        binding = ActivityActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadActivities()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Activity History"
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        activityAdapter = ActivityAdapter(allActivities) { activity ->
            showToast("${activity.type} - ${activity.duration} min")
        }

        binding.rvActivities.layoutManager = LinearLayoutManager(this)
        binding.rvActivities.adapter = activityAdapter
    }

    private fun setupClickListeners() {
        binding.btnToday.setOnClickListener {
            filterActivities("today")
        }

        binding.btnWeek.setOnClickListener {
            filterActivities("week")
        }

        binding.btnMonth.setOnClickListener {
            filterActivities("month")
        }

        binding.btnAll.setOnClickListener {
            filterActivities("all")
        }
    }

    private fun loadActivities() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getActivities(preferencesManager.userId)

                withContext(Dispatchers.Main) {
                    if (response.success) {
                        allActivities.clear()
                        allActivities.addAll(response.getActivities())
                        activityAdapter.updateActivities(allActivities)
                        updateTotalCount(allActivities.size)
                    } else {
                        showToast("Failed to load activities")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLongToast("Error loading activities: ${e.message}")
                }
            }
        }
    }

    private fun filterActivities(filter: String) {
        val calendar = Calendar.getInstance()
        val filtered = when (filter) {
            "today" -> allActivities.filter {
                isSameDay(it.date, calendar.time)
            }
            "week" -> allActivities.filter {
                isWithinWeek(it.date, calendar.time)
            }
            "month" -> allActivities.filter {
                isWithinMonth(it.date, calendar.time)
            }
            else -> allActivities
        }

        activityAdapter.updateActivities(filtered)
        updateButtonSelection(filter)
        updateTotalCount(filtered.size)
    }

    private fun updateButtonSelection(filter: String) {
        binding.btnToday.isSelected = filter == "today"
        binding.btnWeek.isSelected = filter == "week"
        binding.btnMonth.isSelected = filter == "month"
        binding.btnAll.isSelected = filter == "all"
    }

    private fun updateTotalCount(count: Int) {
        binding.tvTotalCount.text = "Showing: $count activities"
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    private fun isWithinWeek(date: Date, reference: Date): Boolean {
        val weekAgo = Calendar.getInstance().apply {
            time = reference
            add(Calendar.DAY_OF_YEAR, -7)
        }
        return date.after(weekAgo.time) || isSameDay(date, weekAgo.time)
    }

    private fun isWithinMonth(date: Date, reference: Date): Boolean {
        val monthAgo = Calendar.getInstance().apply {
            time = reference
            add(Calendar.MONTH, -1)
        }
        return date.after(monthAgo.time) || isSameDay(date, monthAgo.time)
    }
}