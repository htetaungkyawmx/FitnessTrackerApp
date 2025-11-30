package org.hak.fitnesstrackerapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.databinding.ItemWorkoutBinding
import org.hak.fitnesstrackerapp.models.Workout
import org.hak.fitnesstrackerapp.utils.formatDate

class WorkoutAdapter(
    private val onItemClick: (Workout) -> Unit
) : ListAdapter<Workout, WorkoutAdapter.WorkoutViewHolder>(WorkoutDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = getItem(position)
        holder.bind(workout)
    }

    inner class WorkoutViewHolder(
        private val binding: ItemWorkoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(adapterPosition))
                }
            }
        }

        fun bind(workout: Workout) {
            binding.workoutIcon.setImageResource(workout.getIconRes())
            binding.workoutTypeText.text = workout.type.name
            binding.workoutDurationText.text = "${workout.duration} min"
            binding.workoutCaloriesText.text = "${String.format("%.0f", workout.calories)} cal"
            binding.workoutDateText.text = formatDate(workout.date)

            // Set workout-specific details
            when (workout.type) {
                org.hak.fitnesstrackerapp.models.WorkoutType.RUNNING -> {
                    binding.workoutDetailsText.text = "Distance: ${workout.distance} km"
                    binding.workoutDetailsText.visibility = View.VISIBLE
                }
                org.hak.fitnesstrackerapp.models.WorkoutType.CYCLING -> {
                    binding.workoutDetailsText.text = "Distance: ${workout.distance} km"
                    binding.workoutDetailsText.visibility = View.VISIBLE
                }
                org.hak.fitnesstrackerapp.models.WorkoutType.WEIGHTLIFTING -> {
                    binding.workoutDetailsText.text = "${workout.exercises.size} exercises"
                    binding.workoutDetailsText.visibility = View.VISIBLE
                }
            }
        }
    }

    object WorkoutDiffCallback : DiffUtil.ItemCallback<Workout>() {
        override fun areItemsTheSame(oldItem: Workout, newItem: Workout): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Workout, newItem: Workout): Boolean {
            return oldItem == newItem
        }
    }
}
