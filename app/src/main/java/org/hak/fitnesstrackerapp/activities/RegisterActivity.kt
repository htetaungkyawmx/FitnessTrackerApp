package org.hak.fitnesstrackerapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.database.AppDatabase
import org.hak.fitnesstrackerapp.databinding.ActivityRegisterBinding
import org.hak.fitnesstrackerapp.models.User
import org.hak.fitnesstrackerapp.services.NetworkManager
import org.hak.fitnesstrackerapp.utils.PreferenceHelper
import org.hak.fitnesstrackerapp.utils.showToast
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var database: AppDatabase
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var networkManager: NetworkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
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
        binding.registerButton.setOnClickListener {
            attemptRegistration()
        }

        binding.loginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun attemptRegistration() {
        val username = binding.usernameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

        if (validateInput(username, email, password, confirmPassword)) {
            performRegistration(username, email, password)
        }
    }

    private fun validateInput(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (username.isEmpty()) {
            binding.usernameTextInputLayout.error = "Username is required"
            isValid = false
        } else if (username.length < 3) {
            binding.usernameTextInputLayout.error = "Username must be at least 3 characters"
            isValid = false
        } else {
            binding.usernameTextInputLayout.error = null
        }

        if (email.isEmpty()) {
            binding.emailTextInputLayout.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailTextInputLayout.error = "Enter a valid email address"
            isValid = false
        } else {
            binding.emailTextInputLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordTextInputLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordTextInputLayout.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.passwordTextInputLayout.error = null
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordTextInputLayout.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordTextInputLayout.error = "Passwords do not match"
            isValid = false
        } else {
            binding.confirmPasswordTextInputLayout.error = null
        }

        return isValid
    }

    private fun performRegistration(username: String, email: String, password: String) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.registerButton.isEnabled = false

        lifecycleScope.launch {
            try {
                // Check if username already exists locally
                val existingUser = database.userDao().getUserByUsername(username)
                if (existingUser != null) {
                    showToast("Username already exists")
                    return@launch
                }

                // Try network registration
                val result = networkManager.register(username, email, password)
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    user?.let {
                        database.userDao().insertUser(it)
                        preferenceHelper.saveUserSession(it)
                        showToast("Registration successful! Welcome, ${it.username}!")
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finish()
                    }
                } else {
                    // Fallback: Create user locally
                    val newUser = User(
                        id = Random().nextInt(100000),
                        username = username,
                        email = email,
                        password = password
                    )
                    database.userDao().insertUser(newUser)
                    preferenceHelper.saveUserSession(newUser)
                    showToast("Registered locally successfully!")
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finish()
                }
            } catch (e: Exception) {
                showToast("Registration failed: ${e.message}")
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
                binding.registerButton.isEnabled = true
            }
        }
    }
}
