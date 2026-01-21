package org.hak.fitnesstrackerapp.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.adapter.AchievementsAdapter
import org.hak.fitnesstrackerapp.model.Achievement

class AchievementsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvTotalPoints: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvStreak: TextView

    private val TAG = "AchievementsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        // Hide action bar
        supportActionBar?.hide()

        Log.d(TAG, "AchievementsActivity started")

        initializeViews()
        setupClickListeners()
        setupCharts()
        loadAchievements()
        updateStats()
    }

    private fun initializeViews() {
        try {
            btnBack = findViewById(R.id.btnBack)
            tvTitle = findViewById(R.id.tvTitle)
            barChart = findViewById(R.id.barChart)
            pieChart = findViewById(R.id.pieChart)
            recyclerView = findViewById(R.id.recyclerViewAchievements)
            tvTotalPoints = findViewById(R.id.tvTotalPoints)
            tvLevel = findViewById(R.id.tvLevel)
            tvStreak = findViewById(R.id.tvStreak)

            recyclerView.layoutManager = LinearLayoutManager(this)

            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}", e)
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupCharts() {
        setupBarChart()
        setupPieChart()
    }

    private fun setupBarChart() {
        // SIMPLE dummy weekly activity data
        val entries = ArrayList<BarEntry>()

        // Simple data - just 5 days
        val daysOfWeek = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri")
        val minutes = floatArrayOf(30f, 45f, 20f, 60f, 40f)

        for (i in daysOfWeek.indices) {
            entries.add(BarEntry(i.toFloat(), minutes[i]))
        }

        val dataSet = BarDataSet(entries, "Minutes")

        // Simple single color for bars
        dataSet.color = Color.parseColor("#34C759") // Green
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f
        dataSet.setDrawValues(true)

        val barData = BarData(dataSet)
        barData.barWidth = 0.7f
        barData.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}"
            }
        })

        barChart.data = barData

        // X-axis - SIMPLE
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return daysOfWeek.getOrNull(value.toInt()) ?: ""
            }
        }
        xAxis.textSize = 11f
        xAxis.textColor = Color.BLACK

        // Y-axis left - SIMPLE
        val yAxisLeft = barChart.axisLeft
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.gridColor = Color.LTGRAY
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.textSize = 10f
        yAxisLeft.textColor = Color.BLACK

        // Y-axis right - disable
        val yAxisRight = barChart.axisRight
        yAxisRight.isEnabled = false

        // Other chart settings - SIMPLE
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawBorders(false)
        barChart.setTouchEnabled(false) // Disable touch for simplicity
        barChart.setDrawValueAboveBar(true)

        // Simple animation
        barChart.animateY(1000)

        // Refresh chart
        barChart.invalidate()
    }

    private fun setupPieChart() {
        // SIMPLE dummy data - just 3 categories
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(50f, "Running"))
        entries.add(PieEntry(30f, "Gym"))
        entries.add(PieEntry(20f, "Yoga"))

        val dataSet = PieDataSet(entries, "")

        // Simple colors
        dataSet.colors = listOf(
            Color.parseColor("#FF3B30"),  // Red
            Color.parseColor("#007AFF"),  // Blue
            Color.parseColor("#34C759")   // Green
        )

        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE

        val pieData = PieData(dataSet)
        pieChart.data = pieData

        // Simple pie chart settings
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieChart.setDrawHoleEnabled(false) // No hole for simplicity
        pieChart.legend.isEnabled = false

        // Simple animation
        pieChart.animateY(1000)

        pieChart.invalidate()
    }

    private fun loadAchievements() {
        val achievements = generateSimpleDummyAchievements()
        val adapter = AchievementsAdapter(achievements)
        recyclerView.adapter = adapter
    }

    private fun generateSimpleDummyAchievements(): List<Achievement> {
        // Use Android built-in icons or a single default icon
        val defaultIcon = R.drawable.ic_achievement_default

        return listOf(
            Achievement(
                id = 1,
                title = "First Step",
                description = "Complete 1 workout",
                points = 50,
                iconRes = defaultIcon,
                unlocked = true,
                dateUnlocked = "Today"
            ),
            Achievement(
                id = 2,
                title = "Regular Runner",
                description = "3 workouts this week",
                points = 100,
                iconRes = defaultIcon,
                unlocked = true,
                dateUnlocked = "2 days ago"
            ),
            Achievement(
                id = 3,
                title = "Fitness Starter",
                description = "5 total workouts",
                points = 150,
                iconRes = defaultIcon,
                unlocked = false,
                dateUnlocked = null
            ),
            Achievement(
                id = 4,
                title = "Weekly Goal",
                description = "7 days streak",
                points = 200,
                iconRes = defaultIcon,
                unlocked = false,
                dateUnlocked = null
            ),
            Achievement(
                id = 5,
                title = "Distance King",
                description = "Run 10km total",
                points = 250,
                iconRes = defaultIcon,
                unlocked = false,
                dateUnlocked = null
            )
        )
    }

    private fun updateStats() {
        // Simple stats
        tvTotalPoints.text = "850"
        tvLevel.text = "Level 2"
        tvStreak.text = "3 days"
    }

    override fun onResume() {
        super.onResume()
        // Refresh charts
        setupCharts()
    }
}