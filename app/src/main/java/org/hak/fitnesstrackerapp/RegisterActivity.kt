package org.hak.fitnesstrackerapp

import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hak.fitnesstrackerapp.databinding.ActivityRegisterBinding
import org.hak.fitnesstrackerapp.network.RetrofitClient

class RegisterActivity : BaseActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val apiService = RetrofitClient.instance

    override fun setupActivity() {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInput(name, email, password, confirmPassword)) {
                registerUser(name, email, password)
            }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Registering..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userData = mapOf(
                    "name" to name,
                    "email" to email,
                    "password" to password
                )
                val response = apiService.register(userData)

                withContext(Dispatchers.Main) {
                    if (response.success && response.data != null) {
                        // Save user data
                        preferencesManager.isLoggedIn = true
                        preferencesManager.saveUserData(response.data)

                        showToast("Registration successful!")
                        startActivity(Intent(this@RegisterActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        showToast(response.message)
                        binding.btnRegister.isEnabled = true
                        binding.btnRegister.text = "Register"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLongToast("Network error: ${e.message}")
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Register"
                }
            }
        }
    }

    private fun validateInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            return false
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter valid email"
            return false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            return false
        }

        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.error = "Please confirm password"
            return false
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords don't match"
            return false
        }

        return true
    }
}