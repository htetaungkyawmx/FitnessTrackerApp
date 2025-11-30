package org.hak.fitnesstrackerapp.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.databinding.ActivityMainBinding
import org.hak.fitnesstrackerapp.fragments.DashboardFragment
import org.hak.fitnesstrackerapp.fragments.GoalsFragment
import org.hak.fitnesstrackerapp.ui.fragments.HistoryFragment
import org.hak.fitnesstrackerapp.ui.fragments.WorkoutFragment
import org.hak.fitnesstrackerapp.utils.PreferenceHelper

class MainActivity : AppCompatActivity() {

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
                    loadFragment(DashboardFragment())
                    binding.toolbar.title = "Dashboard"
                    true
                }
                R.id.navigation_workout -> {
                    loadFragment(WorkoutFragment())
                    binding.toolbar.title = "Workout"
                    true
                }
                R.id.navigation_goals -> {
                    loadFragment(GoalsFragment())
                    binding.toolbar.title = "Goals"
                    true
                }
                R.id.navigation_history -> {
                    loadFragment(HistoryFragment())
                    binding.toolbar.title = "History"
                    true
                }
                else -> false
            }
        }
    }

    private fun loadDashboardFragment() {
        loadFragment(DashboardFragment())
        binding.bottomNavigation.selectedItemId = R.id.navigation_dashboard
        binding.toolbar.title = "Dashboard"
    }

    private fun loadFragment(fragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } catch (e: Exception) {
            showToast("Error loading screen")
            e.printStackTrace()
        }
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
        super.onBackPressed()
        // Don't exit app on back press, just go to dashboard
        if (binding.bottomNavigation.selectedItemId != R.id.navigation_dashboard) {
            // If not on dashboard, go to dashboard
            binding.bottomNavigation.selectedItemId = R.id.navigation_dashboard
        } else {
            // If already on dashboard, minimize app instead of closing
            moveTaskToBack(true)
        }
        // DON'T call super.onBackPressed() - that would finish the activity
    }
}
