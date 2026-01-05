package org.azm.fitness_app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.azm.fitness_app.R
import org.azm.fitness_app.database.SQLiteHelper
import org.azm.fitness_app.model.User
import org.azm.fitness_app.network.RetrofitClient
import org.azm.fitness_app.utils.SharedPrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etAge: EditText
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var dbHelper: SQLiteHelper
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initDatabase()
        initializeViews()
        setupClickListeners()
    }

    private fun initDatabase() {
        dbHelper = SQLiteHelper(this)
    }

    private fun initializeViews() {
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etAge = findViewById(R.id.etAge)
        etWeight = findViewById(R.id.etWeight)
        etHeight = findViewById(R.id.etHeight)
    }

    private fun setupClickListeners() {
        val btnRegister: Button = findViewById(R.id.btnRegister)
        val btnLogin: Button = findViewById(R.id.btnLogin)

        btnRegister.setOnClickListener {
            registerUser()
        }

        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val age = etAge.text.toString().toIntOrNull() ?: 0
        val weight = etWeight.text.toString().toDoubleOrNull() ?: 0.0
        val height = etHeight.text.toString().toDoubleOrNull() ?: 0.0

        Log.d(TAG, "Registration attempt: name=$name, email=$email, age=$age")

        // Validation
        if (name.isEmpty()) {
            etName.error = "Name is required"
            etName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return
        }

        // Check if user already exists locally
        if (dbHelper.userExists(email)) {
            etEmail.error = "User with this email already exists"
            etEmail.requestFocus()
            Toast.makeText(this, "User with this email already exists", Toast.LENGTH_SHORT).show()
            return
        }

        val user = User(
            id = 0,
            name = name,
            email = email,
            password = password,
            age = age,
            weight = weight,
            height = height
        )

        // Save to server first
        saveUserToServer(user)
    }

    private fun saveUserToServer(user: User) {
        Log.d(TAG, "Attempting to register user on server: ${user.email}")
        Toast.makeText(this, "Registering...", Toast.LENGTH_SHORT).show()

        val call = RetrofitClient.instance.register(user)
        call.enqueue(object : Callback<org.azm.fitness_app.model.LoginResponse> {
            override fun onResponse(
                call: Call<org.azm.fitness_app.model.LoginResponse>,
                response: Response<org.azm.fitness_app.model.LoginResponse>
            ) {
                Log.d(TAG, "Register response: ${response.code()}")

                if (response.isSuccessful) {
                    response.body()?.let { loginResponse ->
                        if (loginResponse.success && loginResponse.user != null) {
                            // Server registration successful
                            val serverUser = loginResponse.user!!

                            // FIX: Create a new user object with the original password
                            val userToSave = User(
                                id = serverUser.id,
                                name = serverUser.name,
                                email = serverUser.email,
                                password = user.password, // Use original password
                                age = serverUser.age,
                                weight = serverUser.weight,
                                height = serverUser.height
                            )

                            Log.d(TAG, "User to save locally: ${userToSave.name}, Password present: ${userToSave.password.isNotEmpty()}")

                            // Save to local database
                            val localId = dbHelper.insertUser(userToSave)

                            if (localId > 0) {
                                val savedUser = userToSave.copy(id = localId.toInt())

                                // Save to SharedPreferences
                                SharedPrefManager.getInstance(this@RegisterActivity).saveUser(savedUser)

                                Log.d(TAG, "User registered successfully: ${savedUser.name}")

                                Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()

                                // Go to main activity
                                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                                finish()
                            } else {
                                Log.e(TAG, "Failed to save user locally")
                                Toast.makeText(this@RegisterActivity, "Failed to save user locally", Toast.LENGTH_SHORT).show()

                                // Try to save locally with different approach
                                saveUserLocally(user)
                            }
                        } else {
                            // Server registration failed
                            Log.w(TAG, "Server registration failed: ${loginResponse.message}")
                            Toast.makeText(
                                this@RegisterActivity,
                                "Registration failed: ${loginResponse.message}",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Try to save locally as fallback
                            saveUserLocally(user)
                        }
                    }
                } else {
                    // Server error
                    Log.w(TAG, "Server error: ${response.code()}")
                    Toast.makeText(this@RegisterActivity, "Server error, saving locally", Toast.LENGTH_SHORT).show()

                    // Save locally as fallback
                    saveUserLocally(user)
                }
            }

            override fun onFailure(call: Call<org.azm.fitness_app.model.LoginResponse>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}")
                Toast.makeText(this@RegisterActivity, "Network error, saving locally", Toast.LENGTH_SHORT).show()

                // Save locally as fallback
                saveUserLocally(user)
            }
        })
    }

    private fun saveUserLocally(user: User) {
        try {
            Log.d(TAG, "Saving user locally: ${user.email}")

            val localId = dbHelper.insertUser(user)

            if (localId > 0) {
                val savedUser = user.copy(id = localId.toInt())

                SharedPrefManager.getInstance(this).saveUser(savedUser)

                Log.d(TAG, "User saved locally with ID: $localId")

                Toast.makeText(this, "Registered locally (offline mode)", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Log.e(TAG, "Failed to save user locally - insert returned: $localId")
                Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user locally: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.closeDB()
    }
}