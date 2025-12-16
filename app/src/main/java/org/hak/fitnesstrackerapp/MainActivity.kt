package org.hak.fitnesstrackerapp

import android.content.Intent
import android.os.Bundle
import org.hak.fitnesstrackerapp.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun setupActivity() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is already logged in
        if (preferencesManager.isLoggedIn) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        /*binding.btnGuest.setOnClickListener {
            preferencesManager.userId = 999
            preferencesManager.userName = "Guest User"
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }*/
    }
}