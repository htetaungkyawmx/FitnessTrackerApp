package org.hak.fitnesstrackerapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.hak.fitnesstrackerapp.databinding.FragmentHistoryBinding
import org.hak.fitnesstrackerapp.utils.showToast

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupSampleData()
    }

    private fun setupClickListeners() {
        binding.filterAll.setOnClickListener {
            showToast("All Workouts Filter")
        }

        binding.filterRunning.setOnClickListener {
            showToast("Running Filter")
        }

        binding.filterCycling.setOnClickListener {
            showToast("Cycling Filter")
        }

        binding.filterWeightlifting.setOnClickListener {
            showToast("Weightlifting Filter")
        }
    }

    private fun setupSampleData() {
        binding.totalWorkoutsStats.text = "12"
        binding.totalCaloriesStats.text = "1,250"
        binding.totalDurationStats.text = "360"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
