package org.hak.fitnesstrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.database.SQLiteHelper
import org.hak.fitnesstrackerapp.network.RetrofitClient
import org.hak.fitnesstrackerapp.utils.SharedPrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var dbHelper: SQLiteHelper
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_login)
            Log.d(TAG, "LoginActivity created successfully")

            dbHelper = SQLiteHelper(this)
            initializeViews()
            setupClickListeners()

            if (SharedPrefManager.getInstance(this).isLoggedIn) {
                Log.d(TAG, "User already logged in, redirecting to MainActivity")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
    }

    private fun setupClickListeners() {
        val btnLogin: Button = findViewById(R.id.btn_login)
        val btnRegister: Button = findViewById(R.id.btn_register)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        btnRegister.setOnClickListener {
            try {
                Log.d(TAG, "Register button clicked, starting RegisterActivity")
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
                Log.d(TAG, "RegisterActivity started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting RegisterActivity: ${e.message}", e)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        Log.d(TAG, "Attempting login for email: $email")
        Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()

        val localUser = dbHelper.authenticateUser(email, password)

        if (localUser != null) {
            SharedPrefManager.getInstance(this).saveUser(localUser)
            Log.d(TAG, "Local login successful: ${localUser.name}")

            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            loginWithServer(email, password)
        }
    }

    private fun loginWithServer(email: String, password: String) {
        try {
            val call = RetrofitClient.instance.login(
                org.hak.fitnesstrackerapp.model.LoginRequest(email, password)
            )

            call.enqueue(object : Callback<org.hak.fitnesstrackerapp.model.LoginResponse> {
                override fun onResponse(
                    call: Call<org.hak.fitnesstrackerapp.model.LoginResponse>,
                    response: Response<org.hak.fitnesstrackerapp.model.LoginResponse>
                ) {
                    Log.d(TAG, "Login response received: ${response.code()}")

                    if (response.isSuccessful && response.body()?.success == true) {
                        response.body()?.user?.let { serverUser ->
                            dbHelper.insertUser(serverUser)

                            SharedPrefManager.getInstance(this@LoginActivity).saveUser(serverUser)

                            Log.d(TAG, "Server login successful: ${serverUser.name}")
                            Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()

                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                    } else {
                        Log.w(TAG, "Login failed: ${response.code()} - ${response.message()}")
                        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<org.hak.fitnesstrackerapp.model.LoginResponse>, t: Throwable) {
                    Log.e(TAG, "Login network error: ${t.message}", t)
                    Toast.makeText(this@LoginActivity, "Network error. Check local users.", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error in server login: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.closeDB()
    }
}