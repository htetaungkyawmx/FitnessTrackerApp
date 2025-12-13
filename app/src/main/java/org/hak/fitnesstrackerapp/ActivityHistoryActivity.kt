package org.hak.fitnesstrackerapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.hak.fitnesstrackerapp.adapters.ActivityAdapter
import org.hak.fitnesstrackerapp.databinding.ActivityActivityHistoryBinding
import org.hak.fitnesstrackerapp.models.FitnessActivity
import java.util.Calendar

class ActivityHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityActivityHistoryBinding
    private lateinit var activityAdapter: ActivityAdapter
    private val allActivities = mutableListOf<FitnessActivity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            Toast.makeText(this, "Activity: ${activity.type}", Toast.LENGTH_SHORT).show()
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
        allActivities.clear()
        allActivities.addAll(generateSampleActivities())
        activityAdapter.notifyDataSetChanged()

        binding.tvTotalCount.text = "Total: ${allActivities.size} activities"
    }

    private fun generateSampleActivities(): List<FitnessActivity> {
        val calendar = Calendar.getInstance()
        val activities = mutableListOf<FitnessActivity>()

        // Today's activities
        activities.add(FitnessActivity(
            id = 1,
            type = "Running",
            duration = 45,
            distance = 5.2,
            calories = 320,
            date = calendar.time,
            note = "Morning run"
        ))

        // Yesterday
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        activities.add(FitnessActivity(
            id = 2,
            type = "Cycling",
            duration = 60,
            distance = 15.5,
            calories = 450,
            date = calendar.time,
            note = "Evening cycling"
        ))

        // 3 days ago
        calendar.add(Calendar.DAY_OF_YEAR, -2)
        activities.add(FitnessActivity(
            id = 3,
            type = "Walking",
            duration = 30,
            distance = 2.5,
            calories = 150,
            date = calendar.time,
            note = "Park walk"
        ))

        // 1 week ago
        calendar.add(Calendar.DAY_OF_YEAR, -4)
        activities.add(FitnessActivity(
            id = 4,
            type = "Swimming",
            duration = 45,
            distance = 1.0,
            calories = 400,
            date = calendar.time,
            note = "Pool session"
        ))

        return activities
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

        activityAdapter = ActivityAdapter(filtered) { activity ->
            Toast.makeText(this, "Activity: ${activity.type}", Toast.LENGTH_SHORT).show()
        }
        binding.rvActivities.adapter = activityAdapter

        // Update UI
        binding.btnToday.isSelected = filter == "today"
        binding.btnWeek.isSelected = filter == "week"
        binding.btnMonth.isSelected = filter == "month"
        binding.btnAll.isSelected = filter == "all"

        binding.tvTotalCount.text = "Showing: ${filtered.size} activities"
    }

    private fun isSameDay(date1: java.util.Date, date2: java.util.Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    private fun isWithinWeek(date: java.util.Date, reference: java.util.Date): Boolean {
        val weekAgo = Calendar.getInstance().apply {
            time = reference
            add(Calendar.DAY_OF_YEAR, -7)
        }
        return date.after(weekAgo.time) || isSameDay(date, weekAgo.time)
    }

    private fun isWithinMonth(date: java.util.Date, reference: java.util.Date): Boolean {
        val monthAgo = Calendar.getInstance().apply {
            time = reference
            add(Calendar.MONTH, -1)
        }
        return date.after(monthAgo.time) || isSameDay(date, monthAgo.time)
    }
}