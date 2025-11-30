package org.hak.fitnesstrackerapp.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.database.AppDatabase
import org.hak.fitnesstrackerapp.databinding.FragmentWorkoutBinding
import org.hak.fitnesstrackerapp.models.Exercise
import org.hak.fitnesstrackerapp.models.Workout
import org.hak.fitnesstrackerapp.models.WorkoutType
import org.hak.fitnesstrackerapp.services.LocationService
import org.hak.fitnesstrackerapp.utils.PreferenceHelper
import org.hak.fitnesstrackerapp.utils.showToast
import java.util.*

class WorkoutFragment : Fragment() {

    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: AppDatabase
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationService: LocationService

    private var isTracking = false
    private var startTime: Long = 0
    private var workoutType: WorkoutType = WorkoutType.RUNNING
    private var workoutTimer: CountDownTimer? = null
    private var elapsedSeconds: Long = 0
    private val exercises = mutableListOf<Exercise>()
    private var currentLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = org.hak.fitnesstrackerapp.FitnessTrackerApp.instance.database
        preferenceHelper = PreferenceHelper(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationService = LocationService(requireContext())

        setupWorkoutTypeSelector()
        setupClickListeners()
        checkLocationPermission()
    }

    private fun setupWorkoutTypeSelector() {
        binding.workoutTypeChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chip_running -> {
                    workoutType = WorkoutType.RUNNING
                    updateUIForWorkoutType()
                }
                R.id.chip_cycling -> {
                    workoutType = WorkoutType.CYCLING
                    updateUIForWorkoutType()
                }
                R.id.chip_weightlifting -> {
                    workoutType = WorkoutType.WEIGHTLIFTING
                    updateUIForWorkoutType()
                }
            }
        }
    }

    private fun updateUIForWorkoutType() {
        when (workoutType) {
            WorkoutType.RUNNING, WorkoutType.CYCLING -> {
                binding.distanceLayout.visibility = View.VISIBLE
                binding.exercisesLayout.visibility = View.GONE
                binding.locationInfo.visibility = View.VISIBLE
            }
            WorkoutType.WEIGHTLIFTING -> {
                binding.distanceLayout.visibility = View.GONE
                binding.exercisesLayout.visibility = View.VISIBLE
                binding.locationInfo.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.startStopButton.setOnClickListener {
            if (!isTracking) {
                startWorkout()
            } else {
                stopWorkout()
            }
        }

        binding.addExerciseButton.setOnClickListener {
            showAddExerciseDialog()
        }

        binding.pauseResumeButton.setOnClickListener {
            togglePauseResume()
        }
    }

    private fun startWorkout() {
        isTracking = true
        startTime = System.currentTimeMillis()
        elapsedSeconds = 0

        binding.startStopButton.text = "STOP"
        binding.startStopButton.setBackgroundColor(resources.getColor(R.color.error, null))
        binding.pauseResumeButton.visibility = View.VISIBLE
        binding.setupLayout.visibility = View.GONE
        binding.trackingLayout.visibility = View.VISIBLE

        startTimer()

        if (workoutType == WorkoutType.RUNNING || workoutType == WorkoutType.CYCLING) {
            startLocationUpdates()
        }

        showToast("Workout started!")
    }

    private fun stopWorkout() {
        isTracking = false
        workoutTimer?.cancel()

        binding.startStopButton.text = "START WORKOUT"
        binding.startStopButton.setBackgroundColor(resources.getColor(R.color.primary, null))
        binding.pauseResumeButton.visibility = View.GONE
        binding.trackingLayout.visibility = View.GONE
        binding.setupLayout.visibility = View.VISIBLE

        locationService.stopLocationUpdates()
        saveWorkout()
    }

    private fun togglePauseResume() {
        if (workoutTimer != null) {
            workoutTimer?.cancel()
            workoutTimer = null
            binding.pauseResumeButton.text = "RESUME"
            showToast("Workout paused")
        } else {
            startTimer()
            binding.pauseResumeButton.text = "PAUSE"
            showToast("Workout resumed")
        }
    }

    private fun startTimer() {
        workoutTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                elapsedSeconds++
                updateTimerDisplay()
            }

            override fun onFinish() {}
        }.start()
    }

    private fun updateTimerDisplay() {
        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60

        binding.timerText.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        // Update calories estimate
        val calories = calculateCalories()
        binding.caloriesText.text = String.format("%.0f cal", calories)
    }

    private fun calculateCalories(): Double {
        // Simple calorie calculation based on time and workout type
        return when (workoutType) {
            WorkoutType.RUNNING -> elapsedSeconds * 0.2
            WorkoutType.CYCLING -> elapsedSeconds * 0.15
            WorkoutType.WEIGHTLIFTING -> elapsedSeconds * 0.1
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
            return
        }

        locationService.startLocationUpdates { location ->
            currentLocation = location
            updateLocationInfo(location)
        }
    }

    private fun updateLocationInfo(location: Location) {
        binding.speedText.text = String.format("%.1f km/h", location.speed * 3.6)
    }

    private fun showAddExerciseDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_exercise, null)
        val exerciseName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.exerciseNameEditText)
        val setsEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.setsEditText)
        val repsEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.repsEditText)
        val weightEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.weightEditText)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Exercise")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, which ->
                val name = exerciseName.text.toString()
                val sets = setsEditText.text.toString().toIntOrNull() ?: 0
                val reps = repsEditText.text.toString().toIntOrNull() ?: 0
                val weight = weightEditText.text.toString().toDoubleOrNull() ?: 0.0

                if (name.isNotEmpty() && sets > 0 && reps > 0) {
                    val exercise = Exercise(name, sets, reps, weight)
                    exercises.add(exercise)
                    updateExercisesList()
                    showToast("Exercise added!")
                } else {
                    showToast("Please fill all fields correctly")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateExercisesList() {
        val exercisesText = exercises.joinToString("\n") { exercise ->
            "• ${exercise.name}: ${exercise.sets}x${exercise.reps} @ ${exercise.weight}kg"
        }
        binding.exercisesText.text = exercisesText
    }

    private fun saveWorkout() {
        lifecycleScope.launch {
            val userId = preferenceHelper.getUserId()
            val duration = (elapsedSeconds / 60).toInt()
            val calories = calculateCalories()

            val workout = when (workoutType) {
                WorkoutType.RUNNING -> createRunningWorkout(userId, duration, calories)
                WorkoutType.CYCLING -> createCyclingWorkout(userId, duration, calories)
                WorkoutType.WEIGHTLIFTING -> createWeightliftingWorkout(userId, duration, calories)
            }

            database.workoutDao().insertWorkout(workout)
            showWorkoutSavedDialog(workout)

            // Reset form
            exercises.clear()
            binding.distanceEditText.setText("")
            binding.exercisesText.text = ""
        }
    }

    private fun createRunningWorkout(userId: Int, duration: Int, calories: Double): Workout {
        val distance = binding.distanceEditText.text.toString().toDoubleOrNull() ?: 0.0
        val averageSpeed = if (duration > 0) distance / (duration / 60.0) else 0.0

        return Workout(
            userId = userId,
            type = WorkoutType.RUNNING,
            duration = duration,
            calories = calories,
            date = Date(),
            distance = distance,
            averageSpeed = averageSpeed,
            notes = binding.notesEditText.text.toString()
        )
    }

    private fun createCyclingWorkout(userId: Int, duration: Int, calories: Double): Workout {
        val distance = binding.distanceEditText.text.toString().toDoubleOrNull() ?: 0.0
        val averageSpeed = if (duration > 0) distance / (duration / 60.0) else 0.0

        return Workout(
            userId = userId,
            type = WorkoutType.CYCLING,
            duration = duration,
            calories = calories,
            date = Date(),
            distance = distance,
            averageSpeed = averageSpeed,
            elevation = null, // Could be calculated from location data
            notes = binding.notesEditText.text.toString()
        )
    }

    private fun createWeightliftingWorkout(userId: Int, duration: Int, calories: Double): Workout {
        return Workout(
            userId = userId,
            type = WorkoutType.WEIGHTLIFTING,
            duration = duration,
            calories = calories,
            date = Date(),
            exercises = exercises.toList(),
            notes = binding.notesEditText.text.toString()
        )
    }

    private fun showWorkoutSavedDialog(workout: Workout) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Workout Saved!")
            .setMessage("${workout.type.name} workout completed!\n\n" +
                    "Duration: ${workout.duration} minutes\n" +
                    "Calories: ${String.format("%.0f", workout.calories)} cal\n" +
                    "${workout.getWorkoutSummary()}")
            .setPositiveButton("Great!") { dialog, which -> }
            .show()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Location permission granted")
            } else {
                showToast("Location permission denied")
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onDestroyView() {
        super.onDestroyView()
        workoutTimer?.cancel()
        locationService.stopLocationUpdates()
        _binding = null
    }
}
