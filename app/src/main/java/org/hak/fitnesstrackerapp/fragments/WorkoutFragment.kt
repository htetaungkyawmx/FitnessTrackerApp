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
    }

    private fun setupClickListeners() {
        binding.startStopButton.setOnClickListener {
            showToast("Start Workout Clicked")
        }

        binding.chipRunning.setOnClickListener {
            showToast("Running Selected")
        }

        binding.chipCycling.setOnClickListener {
            showToast("Cycling Selected")
        }

        binding.chipWeightlifting.setOnClickListener {
            showToast("Weightlifting Selected")
        }

        binding.addExerciseButton.setOnClickListener {
            showToast("Add Exercise Clicked")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
