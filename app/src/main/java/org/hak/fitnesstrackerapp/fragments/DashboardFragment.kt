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

        setupClickListeners()
        setupSampleData()
    }

    private fun setupClickListeners() {
        binding.startWorkoutCard.setOnClickListener {
            showToast("Start Workout Clicked")
        }

        binding.viewGoalsCard.setOnClickListener {
            showToast("View Goals Clicked")
        }

        binding.viewAllWorkouts.setOnClickListener {
            showToast("View All Workouts Clicked")
        }
    }

    private fun setupSampleData() {
        // Set sample data
        binding.totalWorkoutsText.text = "12"
        binding.totalCaloriesText.text = "1,250"
        binding.totalDurationText.text = "360"
        binding.runningDistanceText.text = "25.5"
        binding.cyclingDistanceText.text = "45.2"

        binding.welcomeText.text = "Welcome back!"
        binding.motivationText.text = "Keep up the great work! 💪"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
