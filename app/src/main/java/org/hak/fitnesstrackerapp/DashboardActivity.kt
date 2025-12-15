package org.hak.fitnesstrackerapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hak.fitnesstrackerapp.adapters.ActivityAdapter
import org.hak.fitnesstrackerapp.databinding.ActivityDashboardBinding
import org.hak.fitnesstrackerapp.network.RetrofitClient

class DashboardActivity : BaseActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var activityAdapter: ActivityAdapter
    private val apiService = RetrofitClient.instance

    override fun setupActivity() {
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is logged in
        if (!preferencesManager.isLoggedIn || preferencesManager.userId == -1) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = "Fitness Tracker"

        // Set welcome message
        val userName = preferencesManager.userName
        if (userName.isNotEmpty()) {
            binding.tvWelcome.text = "Welcome, $userName!"
        }
    }

    private fun setupRecyclerView() {
        activityAdapter = ActivityAdapter(emptyList()) { activity ->
            showToast("Selected: ${activity.type}")
        }

        binding.rvActivities.layoutManager = LinearLayoutManager(this)
        binding.rvActivities.adapter = activityAdapter
    }

    private fun setupClickListeners() {
        binding.fabAddActivity.setOnClickListener {
            startActivity(Intent(this, AddActivityActivity::class.java))
        }

        binding.cardSteps.setOnClickListener {
            showToast("Steps: ${binding.tvStepsCount.text}")
        }

        binding.cardCalories.setOnClickListener {
            showToast("Calories: ${binding.tvCaloriesCount.text}")
        }

        binding.cardDistance.setOnClickListener {
            showToast("Distance: ${binding.tvDistanceCount.text}")
        }

        binding.cardDuration.setOnClickListener {
            showToast("Duration: ${binding.tvDurationCount.text}")
        }

        binding.btnViewAllActivities.setOnClickListener {
            startActivity(Intent(this, ActivityHistoryActivity::class.java))
        }
    }

    private fun loadData() {
        loadStats()
        loadRecentActivities()
    }

    private fun loadStats() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getStats(preferencesManager.userId) // Fixed - removed period parameter

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val statsResponse = response.body()
                        if (statsResponse?.success == true) { // Fixed here
                            val stats = statsResponse.getStats() // Fixed here
                            updateStats(stats)
                        } else {
                            showToast(statsResponse?.message ?: "Failed to load stats")
                        }
                    } else {
                        showToast("Server error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLongToast("Error: ${e.message}")
                }
            }
        }
    }

    private fun loadRecentActivities() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getActivities(preferencesManager.userId) // Fixed - removed limit parameter

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val activitiesResponse = response.body()
                        if (activitiesResponse?.success == true) { // Fixed here
                            val activities = activitiesResponse.getActivities() // Fixed here
                            // Show only recent 3 activities
                            val recentActivities = activities.take(3)
                            activityAdapter.updateActivities(recentActivities)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLongToast("Error loading activities")
                }
            }
        }
    }

    private fun updateStats(stats: org.hak.fitnesstrackerapp.models.DashboardStats) {
        binding.tvStepsCount.text = stats.steps.toString()
        binding.tvCaloriesCount.text = "${stats.calories} cal"
        binding.tvDistanceCount.text = String.format("%.1f km", stats.distance)
        binding.tvDurationCount.text = "${stats.duration} min"
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
                showToast("Refreshed!")
                true
            }
            R.id.menu_settings -> {
                showToast("Settings coming soon!")
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