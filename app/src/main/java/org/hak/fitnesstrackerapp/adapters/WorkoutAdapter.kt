package org.hak.fitnesstrackerapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.model.Workout

class WorkoutAdapter(
    private var workouts: List<Workout>,
    private val onItemClick: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvType: TextView = itemView.findViewById(R.id.tvType)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvCalories: TextView = itemView.findViewById(R.id.tvCalories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workouts[position]

        holder.tvType.text = workout.type
        holder.tvDate.text = workout.date
        holder.tvDuration.text = workout.duration.toString() + " min"
        holder.tvCalories.text = workout.calories.toString() + " kcal"

        holder.itemView.setOnClickListener {
            onItemClick(workout)
        }
    }

    override fun getItemCount(): Int = workouts.size

    fun updateData(newWorkouts: List<Workout>) {
        this.workouts = newWorkouts
        notifyDataSetChanged()
    }
}