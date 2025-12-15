package org.hak.fitnesstrackerapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.models.FitnessActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private var activities: List<FitnessActivity>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvHistoryDate)
        val tvActivity: TextView = itemView.findViewById(R.id.tvHistoryActivity)
        val tvDetails: TextView = itemView.findViewById(R.id.tvHistoryDetails)
        val tvCalories: TextView = itemView.findViewById(R.id.tvHistoryCalories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val activity = activities[position]

        // Format date
        val date = activity.getShortDate() // Fixed here

        holder.tvDate.text = date
        holder.tvActivity.text = activity.type

        // Set details based on activity type
        val details = when (activity.type) {
            "Weightlifting" -> {
                activity.exerciseName?.let { exercise ->
                    "${activity.duration} min • $exercise"
                } ?: "${activity.duration} min"
            }
            "Running", "Walking", "Cycling" -> {
                "${activity.duration} min • ${String.format("%.1f km", activity.distance)}"
            }
            else -> "${activity.duration} min"
        }

        holder.tvDetails.text = details
        holder.tvCalories.text = "${activity.calories} cal"
    }

    override fun getItemCount(): Int = activities.size

    fun updateActivities(newActivities: List<FitnessActivity>) {
        this.activities = newActivities
        notifyDataSetChanged()
    }
}