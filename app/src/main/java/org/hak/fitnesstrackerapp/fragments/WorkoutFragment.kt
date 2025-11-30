package org.hak.fitnesstrackerapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.hak.fitnesstrackerapp.databinding.FragmentWorkoutBinding
import org.hak.fitnesstrackerapp.utils.showToast

class WorkoutFragment : Fragment() {

    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!

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

        setupClickListeners()
        setupWorkoutTypeSelector()
    }

    private fun setupWorkoutTypeSelector() {
        binding.chipRunning?.setOnClickListener {
            showToast("Running workout selected")
            updateUIForWorkoutType("RUNNING")
        }

        binding.chipCycling?.setOnClickListener {
            showToast("Cycling workout selected")
            updateUIForWorkoutType("CYCLING")
        }

        binding.chipWeightlifting?.setOnClickListener {
            showToast("Weightlifting workout selected")
            updateUIForWorkoutType("WEIGHTLIFTING")
        }
    }

    private fun updateUIForWorkoutType(type: String) {
        when (type) {
            "RUNNING", "CYCLING" -> {
                binding.distanceLayout?.visibility = View.VISIBLE
                binding.exercisesLayout?.visibility = View.GONE
                binding.locationInfo?.visibility = View.VISIBLE
            }
            "WEIGHTLIFTING" -> {
                binding.distanceLayout?.visibility = View.GONE
                binding.exercisesLayout?.visibility = View.VISIBLE
                binding.locationInfo?.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.startStopButton?.setOnClickListener {
            showToast("Workout started! Let's go! 💪")
            // Simulate workout start
            binding.trackingLayout?.visibility = View.VISIBLE
            binding.setupLayout?.visibility = View.GONE
        }

        binding.addExerciseButton?.setOnClickListener {
            showToast("Add exercise dialog would open here")
        }

        // Set default selection
        binding.chipRunning?.isChecked = true
        updateUIForWorkoutType("RUNNING")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
