package org.hak.fitnesstrackerapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.hak.fitnesstrackerapp.databinding.FragmentDashboardBinding
import org.hak.fitnesstrackerapp.utils.showToast

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // Interface for communication with MainActivity
    interface DashboardListener {
        fun navigateToWorkout()
        fun navigateToGoals()
        fun navigateToHistory()
    }

    private var listener: DashboardListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the listener to parent activity
        listener = activity as? DashboardListener

        setupClickListeners()
        setupSampleData()
    }

    private fun setupClickListeners() {
        // Safe click listeners with null checks
        binding.startWorkoutCard?.setOnClickListener {
            showToast("Start Workout Clicked")
            listener?.navigateToWorkout()
        }

        binding.viewGoalsCard?.setOnClickListener {
            showToast("View Goals Clicked")
            listener?.navigateToGoals()
        }

        binding.viewAllWorkouts?.setOnClickListener {
            showToast("View All Workouts Clicked")
            listener?.navigateToHistory()
        }
    }

    private fun setupSampleData() {
        // Safe data setup with null checks
        binding.totalWorkoutsText?.text = "12"
        binding.totalCaloriesText?.text = "1,250"
        binding.totalDurationText?.text = "360"
        binding.runningDistanceText?.text = "25.5"
        binding.cyclingDistanceText?.text = "45.2"

        binding.welcomeText?.text = "Welcome back!"
        binding.motivationText?.text = "Keep up the great work! 💪"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        listener = null
    }
}
