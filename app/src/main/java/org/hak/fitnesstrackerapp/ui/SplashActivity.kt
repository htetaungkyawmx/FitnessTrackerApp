package org.hak.fitnesstrackerapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.utils.SharedPrefManager

class SplashActivity : AppCompatActivity() {

    private val splashScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        splashScope.launch {
            delay(5000)

            val sharedPrefManager = SharedPrefManager.getInstance(this@SplashActivity)
            val intent = if (sharedPrefManager.isLoggedIn) {
                Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                Intent(this@SplashActivity, LoginActivity::class.java)
            }

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        splashScope.cancel()
    }

    override fun onBackPressed() {

    }
}