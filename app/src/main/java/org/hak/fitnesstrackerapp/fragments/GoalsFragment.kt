package org.hak.fitnesstrackerapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.hak.fitnesstrackerapp.databinding.FragmentGoalsBinding
import org.hak.fitnesstrackerapp.utils.showToast

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupFilterChips()
    }

    private fun setupFilterChips() {
        binding.filterAll?.setOnClickListener {
            showToast("Showing all goals")
        }

        binding.filterActive?.setOnClickListener {
            showToast("Showing active goals")
        }

        binding.filterCompleted?.setOnClickListener {
            showToast("Showing completed goals")
        }
    }

    private fun setupClickListeners() {
        binding.addGoalButton?.setOnClickListener {
            showToast("Add goal dialog would open here")
        }

        binding.fabAddGoal?.setOnClickListener {
            showToast("Add goal FAB clicked")
        }

        binding.createFirstGoalButton?.setOnClickListener {
            showToast("Create your first goal!")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
