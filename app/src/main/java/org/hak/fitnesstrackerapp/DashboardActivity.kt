package org.hak.fitnesstrackerapp

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
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
    private var badgeDot: ImageView? = null

    override fun setupActivity() {
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        setupBadgeDot()
        val userName = preferencesManager.userName
        if (userName.isNotEmpty()) {
            binding.tvWelcome.text = "Welcome, $userName!"
        }
    }

    private fun setupBadgeDot() {
        val profileItem = binding.toolbar.menu?.findItem(R.id.menu_profile)
        if (profileItem != null) {
            val actionView = layoutInflater.inflate(R.layout.menu_badge_layout, null)
            profileItem.actionView = actionView

            badgeDot = actionView.findViewById(R.id.badge_dot)
            val profileIcon = actionView.findViewById<ImageView>(R.id.profile_icon)

            profileIcon.setImageResource(R.drawable.ic_profile)

            actionView.setOnClickListener {
                onOptionsItemSelected(profileItem)
            }
            showBadgeDot(true)
        }
    }

    fun showBadgeDot(show: Boolean) {
        badgeDot?.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
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
                val response = apiService.getStats(preferencesManager.userId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val statsResponse = response.body()
                        if (statsResponse?.success == true) {
                            val stats = statsResponse.getStats()
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
                val response = apiService.getActivities(preferencesManager.userId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val activitiesResponse = response.body()
                        if (activitiesResponse?.success == true) {
                            val activities = activitiesResponse.getActivities()
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

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val profileItem = menu.findItem(R.id.menu_profile)
        if (profileItem != null) {
            val actionView = layoutInflater.inflate(R.layout.menu_badge_layout, null)
            profileItem.actionView = actionView

            badgeDot = actionView.findViewById(R.id.badge_dot)
            val profileIcon = actionView.findViewById<ImageView>(R.id.profile_icon)

            profileIcon.setImageResource(R.drawable.ic_profile)

            actionView.setOnClickListener {
                onOptionsItemSelected(profileItem)
            }
            showBadgeDot(true)
        }

        return super.onPrepareOptionsMenu(menu)
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun performLogout() {
        preferencesManager.clear()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}