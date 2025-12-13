package org.hak.fitnesstrackerapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.hak.fitnesstrackerapp.adapters.ActivityAdapter
import org.hak.fitnesstrackerapp.databinding.ActivityDashboardBinding
import org.hak.fitnesstrackerapp.models.DashboardStats
import org.hak.fitnesstrackerapp.models.FitnessActivity
import org.hak.fitnesstrackerapp.utils.PreferencesManager
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var activityAdapter: ActivityAdapter
    private lateinit var preferencesManager: PreferencesManager
    private val activities = mutableListOf<FitnessActivity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = "Fitness Tracker"
    }

    private fun setupRecyclerView() {
        activityAdapter = ActivityAdapter(activities) { activity ->
            Toast.makeText(this, "Selected: ${activity.type}", Toast.LENGTH_SHORT).show()
        }

        binding.rvActivities.layoutManager = LinearLayoutManager(this)
        binding.rvActivities.adapter = activityAdapter
    }

    private fun setupClickListeners() {
        binding.fabAddActivity.setOnClickListener {
            startActivity(Intent(this, AddActivityActivity::class.java))
        }

        binding.cardSteps.setOnClickListener {
            Toast.makeText(this, "Steps: ${binding.tvStepsCount.text}", Toast.LENGTH_SHORT).show()
        }

        binding.cardCalories.setOnClickListener {
            Toast.makeText(this, "Calories: ${binding.tvCaloriesCount.text}", Toast.LENGTH_SHORT).show()
        }

        binding.cardDistance.setOnClickListener {
            Toast.makeText(this, "Distance: ${binding.tvDistanceCount.text}", Toast.LENGTH_SHORT).show()
        }

        binding.cardDuration.setOnClickListener {
            Toast.makeText(this, "Duration: ${binding.tvDurationCount.text}", Toast.LENGTH_SHORT).show()
        }

        binding.btnViewAllActivities.setOnClickListener {
            startActivity(Intent(this, ActivityHistoryActivity::class.java))
        }
    }

    private fun loadData() {
        val userName = preferencesManager.userName
        if (userName.isNotEmpty()) {
            binding.tvWelcome.text = "Welcome, $userName!"
        }

        val stats = DashboardStats(
            steps = 8452,
            calories = 420,
            distance = 6.8,
            duration = 65
        )

        updateStats(stats)
        loadRecentActivities()
    }

    private fun updateStats(stats: DashboardStats) {
        binding.tvStepsCount.text = stats.steps.toString()
        binding.tvCaloriesCount.text = "${stats.calories} cal"
        binding.tvDistanceCount.text = String.format("%.1f km", stats.distance)
        binding.tvDurationCount.text = "${stats.duration} min"
    }

    private fun loadRecentActivities() {
        activities.clear()
        activities.addAll(generateSampleActivities())
        activityAdapter.notifyDataSetChanged()
    }

    private fun generateSampleActivities(): List<FitnessActivity> {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        return listOf(
            FitnessActivity(
                id = 1,
                type = "Running",
                duration = 45,
                distance = 5.2,
                calories = 320,
                date = calendar.time,
                note = "Morning run in the park"
            ),
            FitnessActivity(
                id = 2,
                type = "Cycling",
                duration = 60,
                distance = 15.5,
                calories = 450,
                date = {
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                    calendar.time
                }(),
                note = "Evening cycling session"
            ),
            FitnessActivity(
                id = 3,
                type = "Walking",
                duration = 30,
                distance = 2.5,
                calories = 150,
                date = {
                    calendar.add(Calendar.DAY_OF_YEAR, -2)
                    calendar.time
                }(),
                note = "Walk with friends"
            )
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.menu_refresh -> {
                loadData()
                Toast.makeText(this, "Refreshed!", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_settings -> {
                Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_logout -> {
                preferencesManager.clear()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}