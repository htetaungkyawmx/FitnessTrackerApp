package org.hak.fitnesstrackerapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.hak.fitnesstrackerapp.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        initViews()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initViews() {
        // Set sample data
        binding.textViewUsername.text = "John Doe"
        binding.textViewEmail.text = "john@example.com"
        binding.textViewMemberSince.text = "Member since: 2024-01-01"

        // Profile information
        binding.textViewHeight.text = "175 cm"
        binding.textViewWeight.text = "70 kg"
        binding.textViewBirthDate.text = "1990-05-15"

        // Statistics
        binding.textViewTotalActivities.text = "25"
        binding.textViewTotalMinutes.text = "1,250"
        binding.textViewTotalCalories.text = "12,500"

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            Toast.makeText(this, "Profile refreshed!", Toast.LENGTH_SHORT).show()
        }

        binding.buttonLogout.setOnClickListener {
            logout()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                Toast.makeText(this, "Edit profile feature coming soon!", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}