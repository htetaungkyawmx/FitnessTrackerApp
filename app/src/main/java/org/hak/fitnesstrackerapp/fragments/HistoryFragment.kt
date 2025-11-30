package org.hak.fitnesstrackerapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.activities.WorkoutDetailActivity
import org.hak.fitnesstrackerapp.database.AppDatabase
import org.hak.fitnesstrackerapp.databinding.FragmentHistoryBinding
import org.hak.fitnesstrackerapp.models.Workout
import org.hak.fitnesstrackerapp.ui.adapters.WorkoutAdapter
import org.hak.fitnesstrackerapp.utils.PreferenceHelper

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: AppDatabase
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var workoutAdapter: WorkoutAdapter

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

        database = org.hak.fitnesstrackerapp.FitnessTrackerApp.instance.database
        preferenceHelper = PreferenceHelper(requireContext())

        setupRecyclerView()
        loadWorkoutHistory()
        setupFilterListeners()
    }

    private fun setupRecyclerView() {
        workoutAdapter = WorkoutAdapter { workout ->
            showWorkoutDetails(workout)
        }

        binding.workoutsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = workoutAdapter
            addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
    }

    private fun loadWorkoutHistory() {
        lifecycleScope.launch {
            val userId = preferenceHelper.getUserId()
            database.workoutDao().getWorkoutsByUser(userId).collectLatest { workouts ->
                if (workouts.isNotEmpty()) {
                    workoutAdapter.submitList(workouts)
                    binding.emptyStateLayout.visibility = View.GONE
                    binding.workoutsRecyclerView.visibility = View.VISIBLE

                    // Update stats
                    val totalWorkouts = workouts.size
                    val totalCalories = workouts.sumOf { it.calories }
                    val totalDuration = workouts.sumOf { it.duration }

                    binding.totalWorkoutsStats.text = totalWorkouts.toString()
                    binding.totalCaloriesStats.text = String.format("%.0f", totalCalories)
                    binding.totalDurationStats.text = "$totalDuration min"
                } else {
                    binding.emptyStateLayout.visibility = View.VISIBLE
                    binding.workoutsRecyclerView.visibility = View.GONE
                }
            }
        }
    }

    private fun setupFilterListeners() {
        binding.filterChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            lifecycleScope.launch {
                val userId = preferenceHelper.getUserId()
                when (checkedIds.firstOrNull()) {
                    R.id.filter_all -> {
                        database.workoutDao().getWorkoutsByUser(userId).collectLatest { workouts ->
                            workoutAdapter.submitList(workouts)
                        }
                    }
                    R.id.filter_running -> {
                        database.workoutDao().getWorkoutsByType(userId, "RUNNING").collectLatest { workouts ->
                            workoutAdapter.submitList(workouts)
                        }
                    }
                    R.id.filter_cycling -> {
                        database.workoutDao().getWorkoutsByType(userId, "CYCLING").collectLatest { workouts ->
                            workoutAdapter.submitList(workouts)
                        }
                    }
                    R.id.filter_weightlifting -> {
                        database.workoutDao().getWorkoutsByType(userId, "WEIGHTLIFTING").collectLatest { workouts ->
                            workoutAdapter.submitList(workouts)
                        }
                    }
                }
            }
        }
    }

    private fun showWorkoutDetails(workout: Workout) {
        val intent = Intent(requireContext(), WorkoutDetailActivity::class.java)
        intent.putExtra("workout", workout as java.io.Serializable)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
