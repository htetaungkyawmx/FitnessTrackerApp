package org.azm.fitness_app.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.azm.fitness_app.R
import org.azm.fitness_app.adapters.GoalAdapter
import org.azm.fitness_app.database.SQLiteHelper
import org.azm.fitness_app.model.Goal
import org.azm.fitness_app.network.RetrofitClient
import org.azm.fitness_app.utils.SharedPrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class GoalsActivity : AppCompatActivity() {
    private lateinit var dbHelper: SQLiteHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateView: View
    private lateinit var btnAddGoal: Button
    private lateinit var goalAdapter: GoalAdapter
    private lateinit var toolbar: Toolbar
    private val TAG = "GoalsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        setupToolbar()
        initDatabase()
        initViews()
        setupRecyclerView()
        loadGoals()
        setupClickListeners()
    }

    private fun setupToolbar() {
        toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "My Goals"
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun initDatabase() {
        dbHelper = SQLiteHelper(this)
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewGoals)
        emptyStateView = findViewById(R.id.emptyStateView)
        btnAddGoal = findViewById(R.id.btnAddGoal)
    }

    private fun setupRecyclerView() {
        goalAdapter = GoalAdapter(emptyList()) { goal ->
            showGoalDetails(goal)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = goalAdapter
    }

    private fun loadGoals() {
        val userId = SharedPrefManager.getInstance(this).user?.id ?: return

        try {
            val goals = dbHelper.getAllGoals(userId)

            if (goals.isEmpty()) {
                showEmptyState()
            } else {
                showGoalList()
                goalAdapter.updateData(goals)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading goals: ${e.message}")
            Toast.makeText(this, "Error loading goals", Toast.LENGTH_SHORT).show()
            showEmptyState()
        }
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyStateView.visibility = View.VISIBLE
    }

    private fun showGoalList() {
        recyclerView.visibility = View.VISIBLE
        emptyStateView.visibility = View.GONE
    }

    private fun setupClickListeners() {
        btnAddGoal.setOnClickListener {
            showAddGoalDialog()
        }

        val btnAddFirst = findViewById<Button>(R.id.btnAddFirst)
        btnAddFirst?.setOnClickListener {
            showAddGoalDialog()
        }
    }

    private fun showAddGoalDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_goal, null)

        val spGoalType = dialogView.findViewById<Spinner>(R.id.spGoalType)
        val etTarget = dialogView.findViewById<EditText>(R.id.etTarget)
        val etDeadline = dialogView.findViewById<EditText>(R.id.etDeadline)

        // Set default deadline (30 days from now)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 30)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        etDeadline.setText(sdf.format(calendar.time))

        // Date picker setup
        etDeadline.setOnClickListener {
            showDatePickerDialog(etDeadline)
        }

        // Goal type spinner setup
        val goalTypes = arrayOf(
            "Weekly Exercise Minutes",
            "Calories Burned",
            "Weight Loss",
            "Distance",
            "Workout Count"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, goalTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spGoalType.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setTitle("Set New Goal")
            .setView(dialogView)
            .setPositiveButton("Set Goal") { dialogInterface, which ->
                val type = spGoalType.selectedItem.toString()
                val targetText = etTarget.text.toString()
                val deadline = etDeadline.text.toString()

                if (type.isEmpty() || targetText.isEmpty() || deadline.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val target = targetText.toDoubleOrNull()
                if (target == null || target <= 0) {
                    Toast.makeText(this, "Please enter a valid target number", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                addNewGoal(type, target, deadline)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun showDatePickerDialog(etDate: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                etDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }.show()
    }

    private fun addNewGoal(type: String, target: Double, deadline: String) {
        val userId = SharedPrefManager.getInstance(this).user?.id ?: return

        // Create new goal
        val goal = Goal(
            userId = userId,
            type = getGoalTypeFromDisplay(type),
            target = target,
            current = 0.0,
            deadline = deadline,
            achieved = false
        )

        // Add goal to local database
        val goalId = dbHelper.insertGoal(goal)

        if (goalId > 0) {
            // Try to save to server
            saveGoalToServer(goal.copy(id = goalId.toInt()))

            Toast.makeText(this, "Goal set successfully!", Toast.LENGTH_SHORT).show()
            loadGoals()
        } else {
            Toast.makeText(this, "Failed to save goal", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveGoalToServer(goal: Goal) {
        val call = RetrofitClient.instance.addGoal(goal)
        call.enqueue(object : Callback<org.azm.fitness_app.model.ApiResponse> {
            override fun onResponse(
                call: Call<org.azm.fitness_app.model.ApiResponse>,
                response: Response<org.azm.fitness_app.model.ApiResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d(TAG, "Goal saved to server: ${goal.id}")
                } else {
                    Log.w(TAG, "Failed to save goal to server")
                }
            }

            override fun onFailure(call: Call<org.azm.fitness_app.model.ApiResponse>, t: Throwable) {
                Log.e(TAG, "Network error saving goal: ${t.message}")
            }
        })
    }

    private fun getGoalTypeFromDisplay(displayType: String): String {
        return when (displayType) {
            "Weekly Exercise Minutes" -> "weekly_minutes"
            "Calories Burned" -> "calories"
            "Weight Loss" -> "weight_loss"
            "Distance" -> "distance"
            "Workout Count" -> "workout_count"
            else -> displayType.lowercase().replace(" ", "_")
        }
    }

    private fun showGoalDetails(goal: Goal) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_goal_details, null)

        val tvGoalType = dialogView.findViewById<TextView>(R.id.tvGoalType)
        val tvTarget = dialogView.findViewById<TextView>(R.id.tvTarget)
        val tvCurrent = dialogView.findViewById<TextView>(R.id.tvCurrent)
        val tvDeadline = dialogView.findViewById<TextView>(R.id.tvDeadline)
        val tvProgress = dialogView.findViewById<TextView>(R.id.tvProgress)
        val progressBar = dialogView.findViewById<LinearProgressIndicator>(R.id.progressBar)
        val btnUpdateProgress = dialogView.findViewById<Button>(R.id.btnUpdateProgress)
        val btnDeleteGoal = dialogView.findViewById<Button>(R.id.btnDeleteGoal)

        // Display goal details
        tvGoalType.text = getGoalTypeDisplay(goal.type)
        tvTarget.text = "${String.format("%.1f", goal.target)} ${getGoalUnit(goal.type)}"
        tvCurrent.text = "${String.format("%.1f", goal.current)} ${getGoalUnit(goal.type)}"
        tvDeadline.text = "Deadline: ${goal.deadline}"

        // Calculate progress
        val progress = if (goal.target > 0) {
            ((goal.current / goal.target) * 100).toInt()
        } else {
            0
        }

        tvProgress.text = "$progress%"
        progressBar.progress = progress.coerceIn(0, 100)

        // Update progress button
        btnUpdateProgress.setOnClickListener {
            showUpdateProgressDialog(goal)
        }

        // Delete goal button
        btnDeleteGoal.setOnClickListener {
            showDeleteConfirmationDialog(goal)
        }

        AlertDialog.Builder(this)
            .setTitle("Goal Details")
            .setView(dialogView)
            .setPositiveButton("Close") { dialog, which ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showUpdateProgressDialog(goal: Goal) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_progress, null)

        val etCurrent = dialogView.findViewById<EditText>(R.id.etCurrent)
        val tvGoalInfo = dialogView.findViewById<TextView>(R.id.tvGoalInfo)

        etCurrent.setText(goal.current.toString())
        tvGoalInfo.text = "${getGoalTypeDisplay(goal.type)} - Target: ${goal.target} ${getGoalUnit(goal.type)}"

        AlertDialog.Builder(this)
            .setTitle("Update Progress")
            .setView(dialogView)
            .setPositiveButton("Update") { dialog, which ->
                val currentText = etCurrent.text.toString()
                val current = currentText.toDoubleOrNull()

                if (current != null && current >= 0) {
                    // Update in local database
                    dbHelper.updateGoalProgress(goal.id, current)

                    // Refresh goals list
                    loadGoals()

                    Toast.makeText(this, "Progress updated!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteConfirmationDialog(goal: Goal) {
        AlertDialog.Builder(this)
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete this goal?")
            .setPositiveButton("Delete") { dialog, which ->
                val rowsAffected = dbHelper.deleteGoal(goal.id)
                if (rowsAffected > 0) {
                    Toast.makeText(this, "Goal deleted", Toast.LENGTH_SHORT).show()
                    loadGoals()
                } else {
                    Toast.makeText(this, "Failed to delete goal", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getGoalTypeDisplay(type: String): String {
        return when (type) {
            "weekly_minutes" -> "Weekly Exercise Minutes"
            "calories" -> "Calories Burned"
            "weight_loss" -> "Weight Loss"
            "distance" -> "Distance"
            "workout_count" -> "Workout Count"
            else -> type.replace("_", " ").split(" ").joinToString(" ") { it.capitalize() }
        }
    }

    private fun getGoalUnit(type: String): String {
        return when (type) {
            "weekly_minutes" -> "minutes"
            "calories" -> "calories"
            "weight_loss" -> "kg"
            "distance" -> "km"
            "workout_count" -> "workouts"
            else -> ""
        }
    }

    override fun onResume() {
        super.onResume()
        loadGoals()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.closeDB()
    }
}