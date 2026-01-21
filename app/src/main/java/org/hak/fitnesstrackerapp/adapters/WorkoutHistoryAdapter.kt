package org.hak.fitnesstrackerapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.model.Workout

class WorkoutHistoryAdapter(
    private val workouts: List<Workout>,
    private val onItemClick: (Workout) -> Unit = {}
) : RecyclerView.Adapter<WorkoutHistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvCalories: TextView = itemView.findViewById(R.id.tvCalories)
        val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workouts[position]

        holder.tvDate.text = workout.date
        holder.tvDuration.text = "${workout.duration} min"
        holder.tvCalories.text = "${workout.calories} cal"

        if (workout.distance != null && workout.distance > 0) {
            holder.tvDistance.text = "${String.format("%.2f", workout.distance)} km"
            holder.tvDistance.visibility = View.VISIBLE
        } else {
            holder.tvDistance.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onItemClick(workout)
        }
    }

    override fun getItemCount() = workouts.size
}