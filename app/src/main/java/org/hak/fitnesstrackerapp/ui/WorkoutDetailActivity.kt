package org.hak.fitnesstrackerapp.ui

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.database.SQLiteHelper

class WorkoutDetailActivity : AppCompatActivity() {
    private lateinit var dbHelper: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_detail)

        dbHelper = SQLiteHelper(this)

        val workoutId = intent.getIntExtra("WORKOUT_ID", -1)

        if (workoutId != -1) {
            loadWorkoutDetails(workoutId)
        } else {
            Toast.makeText(this, "Workout not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadWorkoutDetails(workoutId: Int) {
        val workout = dbHelper.getWorkoutById(workoutId)

        if (workout != null) {
            displayWorkoutDetails(workout)
        } else {
            Toast.makeText(this, "Workout not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun displayWorkoutDetails(workout: org.hak.fitnesstrackerapp.model.Workout) {
        findViewById<TextView>(R.id.tvType).text = workout.type
        findViewById<TextView>(R.id.tvDate).text = workout.date
        findViewById<TextView>(R.id.tvDuration).text = "${workout.duration} minutes"

        val distanceTextView = findViewById<TextView>(R.id.tvDistance)
        val distanceLayout = findViewById<android.view.View>(R.id.distanceLayout)

        if (workout.distance != null && workout.distance > 0) {
            distanceTextView.text = "${String.format("%.2f", workout.distance)} km"
            distanceLayout.visibility = android.view.View.VISIBLE
        } else {
            distanceLayout.visibility = android.view.View.GONE
        }

        findViewById<TextView>(R.id.tvCalories).text = "${workout.calories} kcal"

        val timestampTextView = findViewById<TextView>(R.id.tvTimestamp)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        val dateString = sdf.format(java.util.Date(workout.timestamp))
        timestampTextView.text = "Recorded: $dateString"

        val syncStatusTextView = findViewById<TextView>(R.id.tvSyncStatus)
        syncStatusTextView.text = if (workout.synced) "Synced" else "Not Synced"
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.closeDB()
    }
}