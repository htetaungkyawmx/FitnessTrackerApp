package org.hak.fitnesstrackerapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.hak.fitnesstrackerapp.adapter.ActivityAdapter
import org.hak.fitnesstrackerapp.databinding.ActivityDashboardBinding
import org.hak.fitnesstrackerapp.viewmodel.DashboardViewModel

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var viewModel: DashboardViewModel
    private lateinit var adapter: ActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        setupToolbar()
        initViews()
        // setupObservers()
        // loadData()

        // Set sample data for testing
        setSampleData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Fitness Dashboard"
    }

    private fun initViews() {
        // Setup RecyclerView
        adapter = ActivityAdapter()
        binding.rvRecentActivities.layoutManager = LinearLayoutManager(this)
        binding.rvRecentActivities.adapter = adapter

        // Setup click listeners
        binding.fabAddActivity.setOnClickListener {
            startActivity(Intent(this, AddActivityActivity::class.java))
        }

        binding.cardViewActivities.setOnClickListener {
            startActivity(Intent(this, ActivityHistoryActivity::class.java))
        }

        binding.cardViewStats.setOnClickListener {
            Toast.makeText(this, "Stats feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            Toast.makeText(this, "Refreshed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setSampleData() {
        // Set sample statistics
        binding.tvTotalActivities.text = "15"
        binding.tvTotalMinutes.text = "450"
        binding.tvTotalCalories.text = "3200"

        // Show loading as false
        binding.progressBar.visibility = android.view.View.GONE
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        // Navigate to login
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
