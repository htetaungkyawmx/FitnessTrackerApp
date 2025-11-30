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
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun syncData() {
        showToast("Syncing data...")
        // Implement data synchronization with backend
    }

    private fun logout() {
        preferenceHelper.clearSession()
        showToast("Logged out successfully")
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
