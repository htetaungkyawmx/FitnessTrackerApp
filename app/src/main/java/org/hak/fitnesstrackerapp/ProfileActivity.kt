package org.hak.fitnesstrackerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.hak.fitnesstrackerapp.databinding.ActivityProfileBinding
import org.hak.fitnesstrackerapp.utils.PreferencesManager

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        setupToolbar()
        loadUserData()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun loadUserData() {
        binding.etName.setText(preferencesManager.userName)
        binding.etEmail.setText(preferencesManager.userEmail)
        binding.etDailyGoal.setText(preferencesManager.dailyGoal.toString())
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveProfile()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun saveProfile() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val dailyGoal = binding.etDailyGoal.text.toString().toIntOrNull() ?: 10000

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            return
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            return
        }

        preferencesManager.userName = name
        preferencesManager.userEmail = email
        preferencesManager.dailyGoal = dailyGoal

        Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun logout() {
        preferencesManager.clear()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}