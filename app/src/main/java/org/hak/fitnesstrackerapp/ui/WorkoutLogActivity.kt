package org.hak.fitnesstrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.adapters.WorkoutAdapter
import org.hak.fitnesstrackerapp.database.SQLiteHelper
import org.hak.fitnesstrackerapp.utils.SharedPrefManager

class WorkoutLogActivity : AppCompatActivity() {
    private lateinit var dbHelper: SQLiteHelper
    private lateinit var tvTotalWorkouts: TextView
    private lateinit var tvTotalDuration: TextView
    private lateinit var tvTotalCalories: TextView
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var emptyStateView: android.view.View
    private lateinit var btnAddWorkout: Button
    private lateinit var spSort: Spinner
    private lateinit var workoutAdapter: WorkoutAdapter
    private var currentUserId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_log)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dbHelper = SQLiteHelper(this)

        val user = SharedPrefManager.getInstance(this).user
        if (user != null) {
            currentUserId = user.id
        } else {
            finish()
            return
        }

        initViews()
        setupRecyclerView()
        setupSortSpinner()
        loadWorkoutData()

        btnAddWorkout.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun initViews() {
        tvTotalWorkouts = findViewById(R.id.tvTotalWorkouts)
        tvTotalDuration = findViewById(R.id.tvTotalDuration)
        tvTotalCalories = findViewById(R.id.tvTotalCalories)
        recyclerView = findViewById(R.id.recyclerViewWorkouts)
        emptyStateView = findViewById(R.id.emptyStateView)
        btnAddWorkout = findViewById(R.id.btnAddWorkout)
        spSort = findViewById(R.id.spSort)
    }

    private fun setupRecyclerView() {
        workoutAdapter = WorkoutAdapter(emptyList()) { workout ->
            val intent = Intent(this, WorkoutDetailActivity::class.java)
            intent.putExtra("WORKOUT_ID", workout.id)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = workoutAdapter
    }

    private fun setupSortSpinner() {
        val sortOptions = arrayOf("Newest First", "Oldest First", "Duration (High to Low)", "Calories (High to Low)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSort.adapter = adapter

        spSort.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                loadWorkoutData()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun loadWorkoutData() {
        if (currentUserId == 0) return

        val allWorkouts = dbHelper.getAllWorkouts(currentUserId)

        val sortedWorkouts = when (spSort.selectedItemPosition) {
            0 -> allWorkouts.sortedByDescending { it.timestamp }
            1 -> allWorkouts.sortedBy { it.timestamp }
            2 -> allWorkouts.sortedByDescending { it.duration }
            3 -> allWorkouts.sortedByDescending { it.calories }
            else -> allWorkouts
        }

        val totalWorkouts = dbHelper.getTotalWorkoutsCount(currentUserId)
        val totalDuration = dbHelper.getTotalDuration(currentUserId)
        val totalCalories = dbHelper.getTotalCalories(currentUserId)

        tvTotalWorkouts.text = totalWorkouts.toString()
        tvTotalDuration.text = totalDuration.toString()
        tvTotalCalories.text = totalCalories.toString()

        if (sortedWorkouts.isEmpty()) {
            showEmptyState()
        } else {
            showWorkoutList()
            workoutAdapter.updateData(sortedWorkouts)
        }
    }

    private fun showEmptyState() {
        recyclerView.visibility = android.view.View.GONE
        emptyStateView.visibility = android.view.View.VISIBLE
    }

    private fun showWorkoutList() {
        recyclerView.visibility = android.view.View.VISIBLE
        emptyStateView.visibility = android.view.View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        loadWorkoutData()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.closeDB()
    }
}