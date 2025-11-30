package org.hak.fitnesstrackerapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.databinding.ActivityMainBinding
import org.hak.fitnesstrackerapp.fragments.DashboardFragment
import org.hak.fitnesstrackerapp.fragments.GoalsFragment
import org.hak.fitnesstrackerapp.ui.fragments.HistoryFragment
import org.hak.fitnesstrackerapp.ui.fragments.WorkoutFragment
import org.hak.fitnesstrackerapp.utils.PreferenceHelper

class MainActivity : AppCompatActivity(), DashboardFragment.DashboardListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceHelper = PreferenceHelper(this)

        // Check if user is logged in
        if (!preferenceHelper.isLoggedIn()) {
            navigateToLogin()
            return
        }

        setupToolbar()
        setupNavigation()
        loadDashboardFragment()

        showToast("MainActivity loaded successfully! 🎉")
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Fitness Tracker"

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.action_settings -> {
                    showToast("Settings feature coming soon")
                    true
                }
                R.id.action_sync -> {
                    syncData()
                    true
                }
                R.id.action_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    loadFragment(DashboardFragment(), "Dashboard")
                    binding.toolbar.title = "Dashboard"
                    true
                }
                R.id.navigation_workout -> {
                    loadFragment(WorkoutFragment(), "Workout")
                    binding.toolbar.title = "Workout"
                    true
                }
                R.id.navigation_goals -> {
                    loadFragment(GoalsFragment(), "Goals")
                    binding.toolbar.title = "Goals"
                    true
                }
                R.id.navigation_history -> {
                    loadFragment(HistoryFragment(), "History")
                    binding.toolbar.title = "History"
                    true
                }
                else -> false
            }
        }
    }

    private fun loadDashboardFragment() {
        loadFragment(DashboardFragment(), "Dashboard")
        binding.bottomNavigation.selectedItemId = R.id.navigation_dashboard
        binding.toolbar.title = "Dashboard"
    }

    private fun loadFragment(fragment: Fragment, name: String) {
        try {
            // Check if fragment container exists
            if (findViewById<View>(R.id.fragment_container) == null) {
                showToast("ERROR: Fragment container not found!")
                return
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()

            showToast("$name loaded successfully! ✅")
        } catch (e: Exception) {
            showToast("Error loading $name: ${e.message}")
            e.printStackTrace()
        }
    }

    // Implement DashboardListener interface methods
    override fun navigateToWorkout() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_workout
    }

    override fun navigateToGoals() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_goals
    }

    override fun navigateToHistory() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_history
    }

    private fun syncData() {
        showToast("Syncing data...")
    }

    private fun logout() {
        preferenceHelper.clearSession()
        showToast("Logged out successfully")
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if (binding.bottomNavigation.selectedItemId != R.id.navigation_dashboard) {
            binding.bottomNavigation.selectedItemId = R.id.navigation_dashboard
        } else {
            moveTaskToBack(true)
        }
    }
}
