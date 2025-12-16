package org.hak.fitnesstrackerapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
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
    private var selectedGender = ""

    override fun setupActivity() {
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupGenderSpinner()
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

    private fun setupGenderSpinner() {
        // Gender options
        val genders = arrayOf("Select Gender", "Male", "Female", "Other", "Prefer not to say")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spGender.adapter = adapter

        // Set default selection based on saved gender
        val currentGender = preferencesManager.userGender.trim()
        val position = when (currentGender.lowercase()) {
            "male" -> 1
            "female" -> 2
            "other" -> 3
            "prefer not to say" -> 4
            else -> 0
        }

        if (position < adapter.count) {
            binding.spGender.setSelection(position)
            selectedGender = if (position > 0) currentGender else ""
        }
    }

    private fun loadUserData() {
        try {
            binding.etName.setText(preferencesManager.userName)
            binding.etEmail.setText(preferencesManager.userEmail)

            // Handle age
            val age = if (preferencesManager.userAge > 0) preferencesManager.userAge.toString() else ""
            binding.etAge.setText(age)

            // Handle weight
            val weight = if (preferencesManager.userWeight > 0) preferencesManager.userWeight.toString() else ""
            binding.etWeight.setText(weight)

            // Handle height
            val height = if (preferencesManager.userHeight > 0) preferencesManager.userHeight.toString() else ""
            binding.etHeight.setText(height)

            binding.etDailyGoal.setText(preferencesManager.dailyGoal.toString())

        } catch (e: Exception) {
            showToast("Error loading profile: ${e.message}")
            e.printStackTrace()
        }
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
        try {
            val name = binding.etName.text?.toString()?.trim() ?: ""
            val email = binding.etEmail.text?.toString()?.trim() ?: ""
            val ageText = binding.etAge.text?.toString()?.trim() ?: ""
            val weightText = binding.etWeight.text?.toString()?.trim() ?: ""
            val heightText = binding.etHeight.text?.toString()?.trim() ?: ""
            val dailyGoalText = binding.etDailyGoal.text?.toString()?.trim() ?: "10000"

            val age = ageText.toIntOrNull() ?: 0
            val weight = weightText.toFloatOrNull() ?: 0f
            val height = heightText.toFloatOrNull() ?: 0f
            val dailyGoal = dailyGoalText.toIntOrNull() ?: 10000

            // Get selected gender from spinner
            val genderIndex = binding.spGender.selectedItemPosition
            selectedGender = when (genderIndex) {
                1 -> "Male"
                2 -> "Female"
                3 -> "Other"
                4 -> "Prefer not to say"
                else -> ""
            }

            // Validation
            val errors = mutableListOf<String>()

            if (name.isEmpty()) {
                binding.etName.error = "Name is required"
                errors.add("Name")
            } else {
                binding.etName.error = null
            }

            if (email.isEmpty()) {
                binding.etEmail.error = "Email is required"
                errors.add("Email")
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Enter valid email"
                errors.add("Valid email")
            } else {
                binding.etEmail.error = null
            }

            if (age < 0 || age > 120) {
                binding.etAge.error = "Enter valid age (0-120)"
                errors.add("Valid age")
            } else {
                binding.etAge.error = null
            }

            if (weight < 0 || weight > 300) {
                binding.etWeight.error = "Enter valid weight (0-300 kg)"
                errors.add("Valid weight")
            } else {
                binding.etWeight.error = null
            }

            if (height < 0 || height > 250) {
                binding.etHeight.error = "Enter valid height (0-250 cm)"
                errors.add("Valid height")
            } else {
                binding.etHeight.error = null
            }

            if (dailyGoal < 0 || dailyGoal > 50000) {
                binding.etDailyGoal.error = "Enter valid daily goal (0-50000)"
                errors.add("Valid daily goal")
            } else {
                binding.etDailyGoal.error = null
            }

            if (selectedGender.isEmpty()) {
                showToast("Please select gender")
                errors.add("Gender")
            }

            if (errors.isNotEmpty()) {
                return
            }

            // Save to local preferences first
            preferencesManager.userName = name
            preferencesManager.userEmail = email
            preferencesManager.userAge = age
            preferencesManager.userWeight = weight
            preferencesManager.userHeight = height
            preferencesManager.userGender = selectedGender
            preferencesManager.dailyGoal = dailyGoal

            // Update to server
            updateProfileToServer(name, email, age, weight, height, selectedGender, dailyGoal)

        } catch (e: Exception) {
            showLongToast("Error saving profile: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun updateProfileToServer(
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
                val requestBody = mapOf(
                    "user_id" to preferencesManager.userId,
                    "name" to name,
                    "email" to email,
                    "age" to age,
                    "weight" to weight,
                    "height" to height,
                    "gender" to gender,
                    "daily_goal" to dailyGoal
                )

                val response = apiService.updateProfile(requestBody)

                withContext(Dispatchers.Main) {
                    handleUpdateResponse(response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLongToast("Network error: ${e.message}")
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Save Changes"
                }
            }
        }
    }

    private fun handleUpdateResponse(response: retrofit2.Response<org.hak.fitnesstrackerapp.network.BaseResponse>) {
        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse?.success == true) {
                showToast("Profile updated successfully!")

                apiResponse.data?.let { userData ->
                    preferencesManager.saveUserData(userData)
                }
            } else {
                showToast(apiResponse?.message ?: "Failed to update profile")
            }
        } else {
            showToast("Server error: ${response.code()}")
        }

        binding.btnSave.isEnabled = true
        binding.btnSave.text = "Save Changes"
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
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}