package org.hak.fitnesstrackerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hak.fitnesstrackerapp.databinding.ActivityRegisterBinding
import org.hak.fitnesstrackerapp.network.ApiResponse
import org.hak.fitnesstrackerapp.network.RetrofitClient
import org.hak.fitnesstrackerapp.network.UserRequest
import org.hak.fitnesstrackerapp.utils.PreferencesManager
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var preferencesManager: PreferencesManager
    private val apiService = RetrofitClient.instance

    // Store the user input values as class properties
    private var userName: String = ""
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            userName = binding.etName.text.toString().trim()
            userEmail = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInput(userName, userEmail, password, confirmPassword)) {
                registerUser(userName, userEmail, password)
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
                val response = apiService.register(UserRequest(name, email, password))

                withContext(Dispatchers.Main) {
                    handleRegisterResponse(response, name, email)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Register"
                }
            }
        }
    }

    private fun handleRegisterResponse(
        response: Response<ApiResponse>,
        name: String,
        email: String
    ) {
        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse?.success == true) {
                val userData = apiResponse.data as? Map<String, Any>
                if (userData != null) {
                    preferencesManager.isLoggedIn = true
                    preferencesManager.userId = (userData["user_id"] as? Double)?.toInt() ?: 1
                    preferencesManager.userName = (userData["name"] as? String) ?: name
                    preferencesManager.userEmail = (userData["email"] as? String) ?: email

                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
            } else {
                Toast.makeText(this, apiResponse?.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
        }

        binding.btnRegister.isEnabled = true
        binding.btnRegister.text = "Register"
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