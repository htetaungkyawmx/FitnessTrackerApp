package org.hak.fitnesstrackerapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.hak.fitnesstrackerapp.databinding.ActivityWorkoutDetailBinding
import org.hak.fitnesstrackerapp.models.Workout
import org.hak.fitnesstrackerapp.utils.DateUtils

private val ActivityWorkoutDetailBinding.speedTextView: Any

class WorkoutDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutDetailBinding
    private lateinit var workout: Workout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get workout from intent
        workout = intent.getSerializableExtra("workout") as? Workout ?: return

        setupToolbar()
        displayWorkoutDetails()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Workout Details"
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun displayWorkoutDetails() {
        // Set basic workout info
        binding.workoutNameTextView.text = workout.type.name
        binding.workoutTypeTextView.text = workout.type.name
        binding.durationTextView.text = "${workout.duration} minutes"
        binding.caloriesTextView.text = "${workout.calories.toInt()} cal"
        binding.dateTextView.text = DateUtils.formatDate(workout.date.time)

        // Handle optional fields
        workout.distance?.let { distance ->
            binding.distanceTextView.text = "Distance: ${String.format("%.2f", distance)} km"
            binding.distanceTextView.visibility = android.view.View.VISIBLE
        } ?: run {
            binding.distanceTextView.visibility = android.view.View.GONE
        }

        workout.averageSpeed?.let { speed ->
            binding.speedTextView.text = "Avg Speed: ${String.format("%.1f", speed)} km/h"
            binding.speedTextView.visibility = android.view.View.VISIBLE
        } ?: run {
            binding.speedTextView.visibility = android.view.View.GONE
        }

        workout.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            binding.notesTextView.text = notes
        } ?: run {
            binding.notesTextView.text = "No notes available"
        }
    }
}
