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
import android.view.View
import android.animation.ValueAnimator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator

class SplashActivity : AppCompatActivity() {

    private val splashScope = CoroutineScope(Dispatchers.Main)
    private lateinit var loadingDot1: View
    private lateinit var loadingDot2: View
    private lateinit var loadingDot3: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        // Initialize loading dots
        loadingDot1 = findViewById(R.id.loadingDot1)
        loadingDot2 = findViewById(R.id.loadingDot2)
        loadingDot3 = findViewById(R.id.loadingDot3)

        // Start dot animation
        startDotAnimation()

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

    private fun startDotAnimation() {
        val animatorSet = AnimatorSet()

        val dot1Anim = createDotAnimation(loadingDot1, 0)
        val dot2Anim = createDotAnimation(loadingDot2, 200)
        val dot3Anim = createDotAnimation(loadingDot3, 400)

        animatorSet.playTogether(dot1Anim, dot2Anim, dot3Anim)
        animatorSet.duration = 800
        animatorSet.start()
    }

    private fun createDotAnimation(dot: View, startDelay: Long): AnimatorSet {
        val scaleDown = ObjectAnimator.ofFloat(dot, View.SCALE_X, 1f, 0.5f)
        scaleDown.duration = 400
        scaleDown.startDelay = startDelay
        scaleDown.repeatCount = ValueAnimator.INFINITE
        scaleDown.repeatMode = ValueAnimator.REVERSE

        val scaleDownY = ObjectAnimator.ofFloat(dot, View.SCALE_Y, 1f, 0.5f)
        scaleDownY.duration = 400
        scaleDownY.startDelay = startDelay
        scaleDownY.repeatCount = ValueAnimator.INFINITE
        scaleDownY.repeatMode = ValueAnimator.REVERSE

        val set = AnimatorSet()
        set.playTogether(scaleDown, scaleDownY)
        return set
    }

    override fun onDestroy() {
        super.onDestroy()
        splashScope.cancel()
    }

    override fun onBackPressed() {
        // Prevent back button during splash
    }
}