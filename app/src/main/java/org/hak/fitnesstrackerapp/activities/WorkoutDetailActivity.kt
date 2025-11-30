package org.hak.fitnesstrackerapp.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.databinding.ActivityWorkoutDetailBinding
import org.hak.fitnesstrackerapp.models.Workout
import org.hak.fitnesstrackerapp.models.WorkoutType
import org.hak.fitnesstrackerapp.utils.formatDate
import java.text.SimpleDateFormat
import java.util.*

class WorkoutDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        val workout = intent.getSerializableExtra("workout") as? Workout
        workout?.let {
            // Set basic workout information
            binding.workoutTypeTextView.text = it.type.name.replace("_", " ")
            binding.durationTextView.text = "${it.duration} minutes"
            binding.caloriesTextView.text = "${String.format("%.0f", it.calories)} cal"
            binding.dateTextView.text = formatDate(it.date)

            // Set workout-specific details
            when (it.type) {
                WorkoutType.RUNNING -> {
                    binding.extraDetailsLayout.visibility = android.view.View.VISIBLE
                    binding.extraDetailsTitle.text = "Running Details"
                    val details = buildString {
                        append("Distance: ${it.distance ?: 0.0} km\n")
                        append("Average Speed: ${it.averageSpeed ?: 0.0} km/h\n")
                        append("Pace: ${calculatePace(it.duration, it.distance ?: 0.0)} min/km")
                    }
                    binding.extraDetailsText.text = details
                }
                WorkoutType.CYCLING -> {
                    binding.extraDetailsLayout.visibility = android.view.View.VISIBLE
                    binding.extraDetailsTitle.text = "Cycling Details"
                    val details = buildString {
                        append("Distance: ${it.distance ?: 0.0} km\n")
                        append("Average Speed: ${it.averageSpeed ?: 0.0} km/h\n")
                        append("Elevation: ${it.elevation ?: 0} m")
                    }
                    binding.extraDetailsText.text = details
                }
                WorkoutType.WEIGHTLIFTING -> {
                    binding.extraDetailsLayout.visibility = android.view.View.VISIBLE
                    binding.extraDetailsTitle.text = "Weightlifting Details"
                    val exercisesText = if (it.exercises.isNotEmpty()) {
                        it.exercises.joinToString("\n\n") { exercise ->
                            "${exercise.name}:\n" +
                                    "  • Sets: ${exercise.sets}\n" +
                                    "  • Reps: ${exercise.reps}\n" +
                                    "  • Weight: ${exercise.weight} kg\n" +
                                    "  • Volume: ${exercise.getTotalVolume().format(1)} kg"
                        }
                    } else {
                        "No exercises recorded"
                    }
                    binding.extraDetailsText.text = exercisesText
                }
            }

            // Show notes if available
            if (it.notes.isNotEmpty()) {
                binding.notesLayout.visibility = android.view.View.VISIBLE
                binding.notesTextView.text = it.notes
            } else {
                binding.notesLayout.visibility = android.view.View.GONE
            }

            // Calculate and display additional metrics
            displayAdditionalMetrics(it)
        } ?: run {
            // Handle case where workout is null
            finish()
        }
    }

    private fun calculatePace(duration: Int, distance: Double): String {
        return if (distance > 0) {
            val pace = duration / distance
            String.format("%.2f", pace)
        } else {
            "0.00"
        }
    }

    private fun displayAdditionalMetrics(workout: Workout) {
        lifecycleScope.launch {
            // Calculate intensity
            val intensity = calculateIntensity(workout)
            binding.intensityText.text = "Intensity: $intensity"

            // Calculate efficiency
            val efficiency = calculateEfficiency(workout)
            binding.efficiencyText.text = "Efficiency: ${String.format("%.1f", efficiency)} cal/min"

            // Show additional metrics layout
            binding.additionalMetricsLayout.visibility = android.view.View.VISIBLE
        }
    }

    private fun calculateIntensity(workout: Workout): String {
        val caloriesPerMinute = workout.calories / workout.duration
        return when {
            caloriesPerMinute > 10 -> "High"
            caloriesPerMinute > 5 -> "Medium"
            else -> "Low"
        }
    }

    private fun calculateEfficiency(workout: Workout): Double {
        return workout.calories / workout.duration
    }

    companion object {
        fun calculateCaloriesBurned(duration: Int, type: WorkoutType, weight: Double? = null): Double {
            val baseCalories = when (type) {
                WorkoutType.RUNNING -> duration * 10.0
                WorkoutType.CYCLING -> duration * 8.0
                WorkoutType.WEIGHTLIFTING -> duration * 5.0
            }
            return weight?.let { baseCalories * (it / 70.0) } ?: baseCalories
        }
    }
}
