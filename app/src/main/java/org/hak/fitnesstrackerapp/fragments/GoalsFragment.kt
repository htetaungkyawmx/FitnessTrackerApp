package org.hak.fitnesstrackerapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.database.AppDatabase
import org.hak.fitnesstrackerapp.databinding.FragmentGoalsBinding
import org.hak.fitnesstrackerapp.models.Goal
import org.hak.fitnesstrackerapp.models.GoalType
import org.hak.fitnesstrackerapp.ui.adapters.GoalsAdapter
import org.hak.fitnesstrackerapp.utils.DateUtils
import org.hak.fitnesstrackerapp.utils.PreferenceHelper
import org.hak.fitnesstrackerapp.utils.showToast
import java.util.Calendar

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: AppDatabase
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var goalsAdapter: GoalsAdapter

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

        database = org.hak.fitnesstrackerapp.FitnessTrackerApp.instance.database
        preferenceHelper = PreferenceHelper(requireContext())

        setupRecyclerView()
        loadGoals()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        goalsAdapter = GoalsAdapter { goal ->
            showGoalDetails(goal)
        }

        binding.goalsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = goalsAdapter
            addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
    }

    private fun loadGoals() {
        lifecycleScope.launch {
            val userId = preferenceHelper.getUserId()
            database.goalDao().getGoalsByUser(userId).collectLatest { goals ->
                if (goals.isNotEmpty()) {
                    goalsAdapter.submitList(goals)
                    binding.emptyStateLayout.visibility = View.GONE
                    binding.goalsRecyclerView.visibility = View.VISIBLE
                } else {
                    binding.emptyStateLayout.visibility = View.VISIBLE
                    binding.goalsRecyclerView.visibility = View.GONE
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.addGoalButton.setOnClickListener {
            showAddGoalDialog()
        }

        binding.fabAddGoal.setOnClickListener {
            showAddGoalDialog()
        }
    }

    private fun showAddGoalDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_goal, null)
        val goalTitle = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.goalTitleEditText)
        val goalDescription = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.goalDescriptionEditText)
        val targetValue = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.targetValueEditText)
        val goalType = dialogView.findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.goalTypeAutoComplete)
        val unit = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.unitEditText)

        // Setup goal type dropdown
        val goalTypes = GoalType.values().map { it.name }
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, goalTypes)
        goalType.setAdapter(adapter)

        goalType.setOnItemClickListener { parent, view, position, id ->
            // Auto-fill unit based on goal type
            val selectedType = GoalType.values()[position]
            unit.setText(when (selectedType) {
                GoalType.WEIGHT_LOSS -> "kg"
                GoalType.DISTANCE -> "km"
                GoalType.WORKOUT_COUNT -> "workouts"
                GoalType.CALORIES -> "calories"
            })
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create New Goal")
            .setView(dialogView)
            .setPositiveButton("Create") { dialog, which ->
                val title = goalTitle.text.toString()
                val description = goalDescription.text.toString()
                val target = targetValue.text.toString().toDoubleOrNull() ?: 0.0
                val selectedType = GoalType.valueOf(goalType.text.toString())
                val unitText = unit.text.toString()

                if (title.isNotEmpty() && target > 0 && unitText.isNotEmpty()) {
                    createGoal(title, description, target, selectedType, unitText)
                } else {
                    showToast("Please fill all fields correctly")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createGoal(title: String, description: String, targetValue: Double, type: GoalType, unit: String) {
        lifecycleScope.launch {
            val userId = preferenceHelper.getUserId()
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, 1) // Default deadline: 1 month from now

            val goal = Goal(
                userId = userId,
                title = title,
                description = description,
                targetValue = targetValue,
                unit = unit,
                deadline = calendar.time,
                type = type
            )

            database.goalDao().insertGoal(goal)
            showToast("Goal created successfully!")
        }
    }

    private fun showGoalDetails(goal: Goal) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(goal.title)
            .setMessage(
                "${goal.description}\n\n" +
                        "Target: ${goal.targetValue} ${goal.unit}\n" +
                        "Progress: ${goal.currentValue} ${goal.unit}\n" +
                        "Completion: ${String.format("%.1f", goal.getProgressPercentage())}%\n" +
                        "Deadline: ${DateUtils.formatDate(goal.deadline.time)}"
            )
            .setPositiveButton("Update Progress") { dialog, which ->
                showUpdateProgressDialog(goal)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showUpdateProgressDialog(goal: Goal) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_update_progress, null)
        val progressEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.progressEditText)

        progressEditText.setText(goal.currentValue.toString())

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Update Progress")
            .setView(dialogView)
            .setPositiveButton("Update") { dialog, which ->
                val newProgress = progressEditText.text.toString().toDoubleOrNull() ?: goal.currentValue
                updateGoalProgress(goal, newProgress)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateGoalProgress(goal: Goal, newProgress: Double) {
        lifecycleScope.launch {
            val updatedGoal = goal.copy(currentValue = newProgress)
            database.goalDao().updateGoal(updatedGoal)
            showToast("Progress updated!")
        }
    }

    private fun CoroutineScope.showToast(message: String) {
        TODO("Not yet implemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private fun GoalsFragment.showToast(message: String) {}
