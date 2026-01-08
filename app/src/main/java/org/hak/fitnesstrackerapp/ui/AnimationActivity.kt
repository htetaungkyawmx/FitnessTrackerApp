package org.hak.fitnesstrackerapp.ui

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import org.hak.fitnesstrackerapp.R

class AnimationActivity : AppCompatActivity() {

    private lateinit var animationView: LottieAnimationView
    private lateinit var tvWorkoutType: TextView
    private lateinit var tvInstruction: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animation)

        val workoutType = intent.getStringExtra("WORKOUT_TYPE") ?: "Workout"
        val animationRes = intent.getIntExtra("ANIMATION_RES", -1)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "$workoutType Training"

        initViews()

        tvWorkoutType.text = workoutType

        setupAnimation(workoutType, animationRes)

        setInstruction(workoutType)
    }

    private fun initViews() {
        animationView = findViewById(R.id.animationView)
        tvWorkoutType = findViewById(R.id.tvWorkoutType)
        tvInstruction = findViewById(R.id.tvInstruction)
    }

    private fun setupAnimation(workoutType: String, animationRes: Int) {
        val finalAnimationRes = if (animationRes != -1) animationRes else getDefaultAnimation(workoutType)

        if (finalAnimationRes != -1) {
            try {
                animationView.setAnimation(finalAnimationRes)
                animationView.playAnimation()
                animationView.loop(true)
            } catch (e: Exception) {
                Toast.makeText(this, "Error loading animation", Toast.LENGTH_SHORT).show()
                animationView.visibility = android.view.View.GONE
            }
        } else {
            animationView.visibility = android.view.View.GONE
            Toast.makeText(this, "No animation available for $workoutType", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDefaultAnimation(workoutType: String): Int {
        return when (workoutType.lowercase()) {
            "swimming" -> R.raw.swimming_animation
            "yoga" -> R.raw.yoga_animation
            "strength" -> R.raw.strength_animation
            "hiit" -> R.raw.hiit_animation
            "walking" -> R.raw.cycling_animation
            "weightlifting" -> R.raw.strength_animation
            else -> R.raw.running_animation
        }
    }

    private fun setInstruction(workoutType: String) {
        val instruction = when (workoutType.lowercase()) {
            "swimming" -> "• Warm up with 5 minutes of light swimming\n• Focus on technique and breathing\n• Cool down for 5 minutes"
            "yoga" -> "• Start with breathing exercises\n• Follow the poses with proper alignment\n• End with meditation"
            "strength" -> "• Warm up for 5-10 minutes\n• Focus on proper form over weight\n• Rest 60-90 seconds between sets"
            "hiit" -> "• Warm up for 5 minutes\n• Work hard during active intervals\n• Rest during recovery periods"
            "walking" -> "• Maintain a steady pace\n• Keep good posture\n• Swing arms naturally"
            else -> "Follow the animation and maintain proper form throughout your workout."
        }
        tvInstruction.text = instruction
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        if (animationView.isAnimating) {
            animationView.resumeAnimation()
        }
    }

    override fun onPause() {
        super.onPause()
        if (animationView.isAnimating) {
            animationView.pauseAnimation()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}