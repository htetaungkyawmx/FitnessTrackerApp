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
import org.hak.fitnesstrackerapp.models.WorkoutType
import org.hak.fitnesstrackerapp.utils.DateUtils

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
            binding.workoutIcon.setImageResource(getWorkoutIcon(workout.type)) // FIXED: Use helper function
            binding.workoutTypeText.text = workout.type.toString()
            binding.workoutDurationText.text = "${workout.duration} min"
            binding.workoutCaloriesText.text = "${workout.calories} cal"
            binding.workoutDateText.text = DateUtils.formatDate(workout.date)

            // Set workout-specific details
            workout.distance?.let { distance ->
                binding.workoutDetailsText.text = "Distance: ${String.format("%.1f", distance)} km"
                binding.workoutDetailsText.visibility = View.VISIBLE
            } ?: run {
                binding.workoutDetailsText.visibility = View.GONE
            }
        }

        private fun getWorkoutIcon(workoutType: WorkoutType): Int { // FIXED: Helper function
            return when (workoutType) {
                WorkoutType.RUNNING -> R.drawable.ic_running
                WorkoutType.CYCLING -> R.drawable.ic_cycling
                WorkoutType.WEIGHTLIFTING -> R.drawable.ic_weightlifting
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
