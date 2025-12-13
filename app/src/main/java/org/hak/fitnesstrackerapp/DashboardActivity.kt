package org.hak.fitnesstrackerapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hak.fitnesstrackerapp.adapters.ActivityAdapter
import org.hak.fitnesstrackerapp.databinding.ActivityDashboardBinding
import org.hak.fitnesstrackerapp.network.RetrofitClient
import org.hak.fitnesstrackerapp.utils.PreferencesManager
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var activityAdapter: ActivityAdapter
    private lateinit var preferencesManager: PreferencesManager
    private val apiService = RetrofitClient.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

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
    }

    private fun setupRecyclerView() {
        activityAdapter = ActivityAdapter(emptyList()) { activity ->
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

        loadStats()
        loadRecentActivities()
    }

    private fun loadStats() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getStats(preferencesManager.userId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val stats = response.body()
                        if (stats != null) {
                            updateStats(stats)
                        }
                    } else {
                        Toast.makeText(this@DashboardActivity, "Failed to load stats", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadRecentActivities() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getActivities(preferencesManager.userId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val activities = response.body()
                        if (activities != null && activities.isNotEmpty()) {
                            // Show only recent 3 activities
                            val recentActivities = activities.take(3)
                            activityAdapter.updateActivities(recentActivities)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Error loading activities", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Refreshed!", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_settings -> {
                Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_logout -> {
                preferencesManager.clear()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}