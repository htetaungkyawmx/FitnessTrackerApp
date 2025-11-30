package org.hak.fitnesstrackerapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.activities.WorkoutDetailActivity
import org.hak.fitnesstrackerapp.database.AppDatabase
import org.hak.fitnesstrackerapp.databinding.FragmentDashboardBinding
import org.hak.fitnesstrackerapp.models.Workout
import org.hak.fitnesstrackerapp.models.getIconRes
import org.hak.fitnesstrackerapp.utils.DateUtils
import org.hak.fitnesstrackerapp.utils.PreferenceHelper
import org.hak.fitnesstrackerapp.utils.showToast

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: AppDatabase
    private lateinit var preferenceHelper: PreferenceHelper

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

        database = org.hak.fitnesstrackerapp.FitnessTrackerApp.instance.database
        preferenceHelper = PreferenceHelper(requireContext())

        setupCharts()
        loadDashboardData()
        setupClickListeners()
    }

    private fun setupCharts() {
        setupWorkoutChart(binding.workoutChart)
    }

    private fun setupWorkoutChart(chart: LineChart) {
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        chart.axisLeft.setDrawGridLines(false)
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            val userId = preferenceHelper.getUserId()

            // Load stats
            val totalWorkouts = database.workoutDao().getTotalWorkouts(userId)
            val totalCalories = database.workoutDao().getTotalCalories(userId)
            val totalDuration = database.workoutDao().getTotalDuration(userId)
            val runningDistance = database.workoutDao().getTotalRunningDistance(userId)
            val cyclingDistance = database.workoutDao().getTotalCyclingDistance(userId)

            binding.totalWorkoutsText.text = totalWorkouts.toString()
            binding.totalCaloriesText.text = String.format("%.0f", totalCalories)
            binding.totalDurationText.text = "$totalDuration min"
            binding.runningDistanceText.text = String.format("%.1f km", runningDistance)
            binding.cyclingDistanceText.text = String.format("%.1f km", cyclingDistance)

            // Load recent workouts
            database.workoutDao().getWorkoutsByUser(userId).collectLatest { workouts ->
                if (workouts.isNotEmpty()) {
                    val recentWorkouts = workouts.take(3)
                    setupRecentWorkouts(recentWorkouts)
                    setupChartData(workouts.take(7))
                } else {
                    binding.recentWorkoutsLayout.visibility = View.GONE
                    binding.emptyStateLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupRecentWorkouts(workouts: List<Workout>) {
        binding.recentWorkoutsLayout.visibility = View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE

        workouts.forEachIndexed { index, workout ->
            when (index) {
                0 -> {
                    binding.workout1Type.setImageResource(workout.getIconRes())
                    binding.workout1Title.text = workout.type.name
                    binding.workout1Subtitle.text = "${workout.duration} min • ${workout.calories.toInt()} cal"
                    binding.workout1Date.text = DateUtils.formatDate(workout.date.time)
                    binding.workout1Card.setOnClickListener {
                        showWorkoutDetails(workout)
                    }
                }
                1 -> {
                    binding.workout2Type.setImageResource(workout.getIconRes())
                    binding.workout2Title.text = workout.type.name
                    binding.workout2Subtitle.text = "${workout.duration} min • ${workout.calories.toInt()} cal"
                    binding.workout2Date.text = DateUtils.formatDate(workout.date.time)
                    binding.workout2Card.setOnClickListener {
                        showWorkoutDetails(workout)
                    }
                }
                2 -> {
                    binding.workout3Type.setImageResource(workout.getIconRes())
                    binding.workout3Title.text = workout.type.name
                    binding.workout3Subtitle.text = "${workout.duration} min • ${workout.calories.toInt()} cal"
                    binding.workout3Date.text = DateUtils.formatDate(workout.date.time)
                    binding.workout3Card.setOnClickListener {
                        showWorkoutDetails(workout)
                    }
                }
            }
        }
    }

    private fun setupChartData(workouts: List<Workout>) {
        if (workouts.isEmpty()) return

        val entries = ArrayList<Entry>()
        workouts.reversed().forEachIndexed { index, workout ->
            entries.add(Entry(index.toFloat(), workout.calories.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Calories Burned")
        dataSet.color = resources.getColor(R.color.primary, null)
        dataSet.lineWidth = 2f
        dataSet.setCircleColor(resources.getColor(R.color.primary, null))
        dataSet.circleRadius = 4f
        dataSet.setDrawValues(false)

        val lineData = LineData(dataSet)
        binding.workoutChart.data = lineData
        binding.workoutChart.invalidate()
    }

    private fun setupClickListeners() {
        binding.startWorkoutCard.setOnClickListener {
            showToast("Navigate to Workout")
            // Navigate to workout fragment
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.navigation_workout
        }

        binding.viewAllWorkouts.setOnClickListener {
            showToast("View all workouts")
            // Navigate to history
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.navigation_history
        }

        binding.viewGoalsCard.setOnClickListener {
            showToast("View goals")
            // Navigate to goals
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.navigation_goals
        }
    }

    private fun showWorkoutDetails(workout: Workout) {
        val intent = Intent(requireContext(), WorkoutDetailActivity::class.java)
        intent.putExtra("workout", workout)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
