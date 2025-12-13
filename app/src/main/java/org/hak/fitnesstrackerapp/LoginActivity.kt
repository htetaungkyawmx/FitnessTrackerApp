package org.hak.fitnesstrackerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hak.fitnesstrackerapp.databinding.ActivityLoginBinding
import org.hak.fitnesstrackerapp.network.ApiResponse
import org.hak.fitnesstrackerapp.network.LoginRequest
import org.hak.fitnesstrackerapp.network.RetrofitClient
import org.hak.fitnesstrackerapp.utils.PreferencesManager
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferencesManager: PreferencesManager
    private val apiService = RetrofitClient.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        // Auto login if already logged in
        if (preferencesManager.isLoggedIn && preferencesManager.userId != -1) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
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
            Toast.makeText(this, "Reset password feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginUser(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Logging in..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.login(LoginRequest(email, password))

                withContext(Dispatchers.Main) {
                    handleLoginResponse(response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Login"
                }
            }
        }
    }

    private fun handleLoginResponse(response: Response<ApiResponse>) {
        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse?.success == true) {
                val userData = apiResponse.data as? Map<String, Any>
                if (userData != null) {
                    preferencesManager.isLoggedIn = true
                    preferencesManager.userId = (userData["id"] as? Double)?.toInt() ?: 1
                    preferencesManager.userName = (userData["name"] as? String) ?: "User"
                    preferencesManager.userEmail = (userData["email"] as? String) ?: ""

                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid response data", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, apiResponse?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = "Login"
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