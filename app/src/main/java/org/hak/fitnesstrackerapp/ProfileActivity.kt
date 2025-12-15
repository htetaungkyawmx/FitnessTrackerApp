package org.hak.fitnesstrackerapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hak.fitnesstrackerapp.databinding.ActivityProfileBinding
import org.hak.fitnesstrackerapp.network.RetrofitClient

class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val apiService = RetrofitClient.instance

    override fun setupActivity() {
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        binding.etAge.setText(if (preferencesManager.userAge > 0) preferencesManager.userAge.toString() else "")
        binding.etWeight.setText(if (preferencesManager.userWeight > 0) preferencesManager.userWeight.toString() else "")
        binding.etHeight.setText(if (preferencesManager.userHeight > 0) preferencesManager.userHeight.toString() else "")
        binding.etGender.setText(preferencesManager.userGender)
        binding.etDailyGoal.setText(preferencesManager.dailyGoal.toString())
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveProfile()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun saveProfile() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val age = binding.etAge.text.toString().toIntOrNull() ?: 0
        val weight = binding.etWeight.text.toString().toFloatOrNull() ?: 0f
        val height = binding.etHeight.text.toString().toFloatOrNull() ?: 0f
        val gender = binding.etGender.text.toString().trim()
        val dailyGoal = binding.etDailyGoal.text.toString().toIntOrNull() ?: 10000

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            return
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter valid email"
            return
        }

        if (age < 0 || age > 120) {
            binding.etAge.error = "Enter valid age (0-120)"
            return
        }

        if (weight < 0 || weight > 300) {
            binding.etWeight.error = "Enter valid weight (0-300 kg)"
            return
        }

        if (height < 0 || height > 250) {
            binding.etHeight.error = "Enter valid height (0-250 cm)"
            return
        }

        if (dailyGoal < 0 || dailyGoal > 50000) {
            binding.etDailyGoal.error = "Enter valid daily goal (0-50000)"
            return
        }

        // Update profile locally first
        preferencesManager.userName = name
        preferencesManager.userEmail = email
        preferencesManager.userAge = age
        preferencesManager.userWeight = weight
        preferencesManager.userHeight = height
        preferencesManager.userGender = gender
        preferencesManager.dailyGoal = dailyGoal

        // Update profile on server
        updateProfileOnServer(name, email, age, weight, height, gender, dailyGoal)
    }

    private fun updateProfileOnServer(
        name: String,
        email: String,
        age: Int,
        weight: Float,
        height: Float,
        gender: String,
        dailyGoal: Int
    ) {
        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Saving..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val profileData = mapOf(
                    "user_id" to preferencesManager.userId,
                    "name" to name,
                    "email" to email,
                    "age" to age,
                    "weight" to weight,
                    "height" to height,
                    "gender" to gender,
                    "daily_goal" to dailyGoal
                )

                // Note: You need to implement update_profile.php endpoint
                // For now, just show success message
                withContext(Dispatchers.Main) {
                    showToast("Profile saved successfully!")
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Save Changes"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLongToast("Error: ${e.message}")
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Save Changes"
                }
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        preferencesManager.clear()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}