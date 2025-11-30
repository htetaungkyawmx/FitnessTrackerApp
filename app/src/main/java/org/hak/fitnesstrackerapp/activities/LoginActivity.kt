package org.hak.fitnesstrackerapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.database.AppDatabase
import org.hak.fitnesstrackerapp.databinding.ActivityLoginBinding
import org.hak.fitnesstrackerapp.services.NetworkManager
import org.hak.fitnesstrackerapp.ui.activities.MainActivity
import org.hak.fitnesstrackerapp.ui.activities.RegisterActivity
import org.hak.fitnesstrackerapp.utils.PreferenceHelper
import org.hak.fitnesstrackerapp.utils.showToast

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: AppDatabase
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var networkManager: NetworkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getInstance(this)
        preferenceHelper = PreferenceHelper(this)
        networkManager = NetworkManager()

        setupAnimations()
        setupClickListeners()
    }

    private fun setupAnimations() {
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        binding.logoImageView.startAnimation(fadeIn)
        binding.cardView.startAnimation(slideUp)
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            attemptLogin()
        }

        binding.registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.forgotPasswordTextView.setOnClickListener {
            showToast("Password reset feature coming soon")
        }
    }

    private fun attemptLogin() {
        val username = binding.usernameEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (validateInput(username, password)) {
            performLogin(username, password)
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        if (username.isEmpty()) {
            binding.usernameTextInputLayout.error = "Username is required"
            return false
        } else {
            binding.usernameTextInputLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordTextInputLayout.error = "Password is required"
            return false
        } else if (password.length < 6) {
            binding.passwordTextInputLayout.error = "Password must be at least 6 characters"
            return false
        } else {
            binding.passwordTextInputLayout.error = null
        }

        return true
    }

    private fun performLogin(username: String, password: String) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.loginButton.isEnabled = false

        lifecycleScope.launch {
            try {
                // Try local database first
                var user = database.userDao().login(username, password)

                if (user == null) {
                    // Try network login
                    val result = networkManager.login(username, password)
                    if (result.isSuccess) {
                        user = result.getOrNull()
                        user?.let { database.userDao().insertUser(it) }
                    }
                }

                if (user != null) {
                    preferenceHelper.saveUserSession(user)
                    showToast("Welcome back, ${user.username}!")
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    showToast("Invalid credentials")
                }
            } catch (e: Exception) {
                showToast("Login failed: ${e.message}")
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
                binding.loginButton.isEnabled = true
            }
        }
    }
}
