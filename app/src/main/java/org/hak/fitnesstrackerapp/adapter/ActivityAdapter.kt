package org.hak.fitnesstrackerapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.databinding.ItemActivityBinding
import org.hak.fitnesstrackerapp.model.Activity
import org.hak.fitnesstrackerapp.model.ActivityType
import org.hak.fitnesstrackerapp.utils.DateUtils

class ActivityAdapter(
    private val onItemClick: (Activity) -> Unit = {}
) : ListAdapter<Activity, ActivityAdapter.ViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActivityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = getItem(position)
        holder.bind(activity)
    }

    inner class ViewHolder(
        private val binding: ItemActivityBinding,
        private val onItemClick: (Activity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val activity = getItem(adapterPosition)
                    onItemClick(activity)
                }
            }
        }

        fun bind(activity: Activity) {
            // Set activity type with icon
            binding.tvActivityType.text = getActivityDisplayText(activity.type)
            binding.ivActivityIcon.setImageResource(getActivityIcon(activity.type))

            // Set duration
            binding.tvDuration.text = formatDuration(activity.durationMinutes)

            // Set formatted date
            binding.tvDate.text = DateUtils.formatForDisplay(activity.createdAt)

            // Set distance if available
            activity.distanceKm?.let { distance ->
                binding.tvDistance.text = String.format("%.2f km", distance)
                binding.tvDistance.visibility = View.VISIBLE
                binding.ivDistanceIcon.visibility = View.VISIBLE
            } ?: run {
                binding.tvDistance.visibility = View.GONE
                binding.ivDistanceIcon.visibility = View.GONE
            }

            // Set calories if available
            activity.calories?.let { calories ->
                binding.tvCalories.text = "$calories cal"
                binding.tvCalories.visibility = View.VISIBLE
                binding.ivCaloriesIcon.visibility = View.VISIBLE
            } ?: run {
                binding.tvCalories.visibility = View.GONE
                binding.ivCaloriesIcon.visibility = View.GONE
            }

            // Set notes if available
            activity.notes?.takeIf { it.isNotEmpty() }?.let { notes ->
                binding.tvNotes.text = notes
                binding.tvNotes.visibility = View.VISIBLE
            } ?: run {
                binding.tvNotes.visibility = View.GONE
            }

            // Set pace if available (for distance-based activities)
            activity.pace?.let { pace ->
                binding.tvPace.text = pace
                binding.tvPace.visibility = View.VISIBLE
                binding.ivPaceIcon.visibility = View.VISIBLE
            } ?: run {
                binding.tvPace.visibility = View.GONE
                binding.ivPaceIcon.visibility = View.GONE
            }

            // Highlight today's activities
            if (DateUtils.isToday(activity.createdAt)) {
                binding.cardActivity.setCardBackgroundColor(
                    binding.root.context.getColor(android.R.color.holo_blue_light)
                )
            } else {
                binding.cardActivity.setCardBackgroundColor(
                    binding.root.context.getColor(android.R.color.background_light)
                )
            }
        }

        private fun formatDuration(minutes: Int): String {
            return when {
                minutes < 60 -> "$minutes min"
                else -> {
                    val hours = minutes / 60
                    val remainingMinutes = minutes % 60
                    if (remainingMinutes == 0) {
                        "$hours hr"
                    } else {
                        "$hours hr $remainingMinutes min"
                    }
                }
            }
        }

        private fun getActivityDisplayText(activityType: ActivityType): String {
            return when (activityType) {
                ActivityType.Running -> "🏃 Running"
                ActivityType.Cycling -> "🚴 Cycling"
                ActivityType.Weightlifting -> "🏋️ Weightlifting"
                ActivityType.Swimming -> "🏊 Swimming"
                ActivityType.Yoga -> "🧘 Yoga"
                ActivityType.Walking -> "🚶 Walking"
                ActivityType.Other -> "🏅 Other"
            }
        }

        private fun getActivityIcon(activityType: ActivityType): Int {
            return when (activityType) {
                ActivityType.Running -> R.drawable.ic_running
                ActivityType.Cycling -> R.drawable.ic_cycling
                ActivityType.Weightlifting -> R.drawable.ic_weightlifting
                ActivityType.Swimming -> R.drawable.ic_swimming
                ActivityType.Yoga -> R.drawable.ic_yoga
                ActivityType.Walking -> R.drawable.ic_walking
                ActivityType.Other -> R.drawable.ic_other
            }
        }
    }
}

class ActivityDiffCallback : DiffUtil.ItemCallback<Activity>() {
    override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean {
        return oldItem == newItem
    }
}
