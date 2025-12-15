package org.hak.fitnesstrackerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hak.fitnesstrackerapp.databinding.ActivityLoginBinding
import org.hak.fitnesstrackerapp.network.RetrofitClient

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val apiService = RetrofitClient.instance

    override fun setupActivity() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Auto login if already logged in
        if (preferencesManager.isLoggedIn && preferencesManager.userId != -1) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            showToast("Reset password feature coming soon!")
        }
    }

    private fun loginUser(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Logging in..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val credentials = mapOf(
                    "email" to email,
                    "password" to password
                )
                val response = apiService.login(credentials)

                withContext(Dispatchers.Main) {
                    if (response.success && response.data != null) {
                        // Save user data
                        preferencesManager.isLoggedIn = true
                        preferencesManager.saveUserData(response.data)

                        showToast("Login successful!")
                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        showToast(response.message)
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.text = "Login"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLongToast("Network error: ${e.message}")
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Login"
                }
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter valid email"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        return isValid
    }
}