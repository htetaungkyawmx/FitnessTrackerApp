package org.azm.fitness_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.azm.fitness_app.R
import org.azm.fitness_app.model.Goal

class GoalAdapter(
    private var goals: List<Goal>,
    private val onItemClick: (Goal) -> Unit
) : RecyclerView.Adapter<GoalAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvGoalType: TextView = itemView.findViewById(R.id.tvGoalType)
        val tvTarget: TextView = itemView.findViewById(R.id.tvTarget)
        val tvDeadline: TextView = itemView.findViewById(R.id.tvDeadline)
        val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val tvAchieved: TextView = itemView.findViewById(R.id.tvAchieved)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val goal = goals[position]

        // Set goal type
        holder.tvGoalType.text = getGoalTypeDisplay(goal.type)

        // Set target
        holder.tvTarget.text = "Target: ${goal.target} ${getGoalUnit(goal.type)}"

        // Set deadline
        holder.tvDeadline.text = "Deadline: ${goal.deadline}"

        // Calculate and display progress
        val progress = if (goal.target > 0) {
            ((goal.current / goal.target) * 100).toInt()
        } else {
            0
        }

        holder.tvProgress.text = "$progress%"
        holder.progressBar.progress = progress.coerceIn(0, 100)

        // Set progress bar color based on progress
        val progressColor = when {
            goal.achieved -> ContextCompat.getColor(holder.itemView.context, R.color.success)
            progress >= 75 -> ContextCompat.getColor(holder.itemView.context, R.color.secondary)
            progress >= 50 -> ContextCompat.getColor(holder.itemView.context, R.color.primary)
            else -> ContextCompat.getColor(holder.itemView.context, R.color.warning)
        }
        holder.progressBar.progressDrawable.setTint(progressColor)

        // Show achieved badge if goal is achieved
        if (goal.achieved) {
            holder.tvAchieved.visibility = View.VISIBLE
        } else {
            holder.tvAchieved.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onItemClick(goal)
        }
    }

    override fun getItemCount(): Int = goals.size

    fun updateData(newGoals: List<Goal>) {
        this.goals = newGoals
        notifyDataSetChanged()
    }

    private fun getGoalTypeDisplay(type: String): String {
        return when (type) {
            "weekly_minutes" -> "Weekly Exercise"
            "calories" -> "Calories Burned"
            "weight_loss" -> "Weight Loss"
            "distance" -> "Distance"
            "workout_count" -> "Workout Count"
            else -> type.replace("_", " ").capitalizeWords()
        }
    }

    private fun getGoalUnit(type: String): String {
        return when (type) {
            "weekly_minutes" -> "min"
            "calories" -> "cal"
            "weight_loss" -> "kg"
            "distance" -> "km"
            "workout_count" -> "workouts"
            else -> ""
        }
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}