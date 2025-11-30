package org.hak.fitnesstrackerapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.databinding.ItemGoalBinding
import org.hak.fitnesstrackerapp.models.Goal
import org.hak.fitnesstrackerapp.utils.DateUtils

class GoalsAdapter(
    private val onItemClick: (Goal) -> Unit
) : ListAdapter<Goal, GoalsAdapter.GoalViewHolder>(GoalDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = ItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = getItem(position)
        holder.bind(goal)
    }

    inner class GoalViewHolder(
        private val binding: ItemGoalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(adapterPosition))
                }
            }
        }

        fun bind(goal: Goal) {
            binding.goalIcon.setImageResource(goal.getIconRes())
            binding.goalTitleText.text = goal.title
            binding.goalDescriptionText.text = goal.description
            binding.goalProgressText.text = "${goal.currentValue} / ${goal.targetValue} ${goal.unit}"
            binding.goalDeadlineText.text = "Deadline: ${DateUtils.formatDate(goal.deadline)}"

            // Setup progress bar
            val progress = goal.getProgressPercentage().toInt()
            binding.progressBar.progress = progress
            binding.progressPercentageText.text = "$progress%"

            // Change color based on progress
            val progressColor = when {
                progress >= 100 -> R.color.success
                progress >= 75 -> R.color.info
                progress >= 50 -> R.color.warning
                else -> R.color.error
            }

            binding.progressBar.progressTintList = ContextCompat.getColorStateList(binding.root.context, progressColor)

            // Show completion status
            if (goal.isCompleted) {
                binding.completedBadge.visibility = View.VISIBLE
            } else {
                binding.completedBadge.visibility = View.GONE
            }
        }
    }

    object GoalDiffCallback : DiffUtil.ItemCallback<Goal>() {
        override fun areItemsTheSame(oldItem: Goal, newItem: Goal): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Goal, newItem: Goal): Boolean {
            return oldItem == newItem
        }
    }
}
