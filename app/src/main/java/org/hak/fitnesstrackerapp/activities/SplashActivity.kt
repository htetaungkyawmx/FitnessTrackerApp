package org.hak.fitnesstrackerapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.utils.PreferenceHelper

class SplashActivity : AppCompatActivity() {

    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        preferenceHelper = PreferenceHelper(this)

        // Delay for 2 seconds then check login status
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, 2000)
    }

    private fun checkLoginStatus() {
        val intent = if (preferenceHelper.isLoggedIn() && preferenceHelper.isSessionValid()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish() // Close splash activity properly
    }
}
