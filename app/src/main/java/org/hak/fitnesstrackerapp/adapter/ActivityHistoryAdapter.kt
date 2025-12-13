package org.hak.fitnesstrackerapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.hak.fitnesstrackerapp.databinding.ItemActivityHistoryBinding
import org.hak.fitnesstrackerapp.network.models.ActivityResponse
import org.hak.fitnesstrackerapp.R

class ActivityHistoryAdapter(
    private val onDeleteClick: (Int) -> Unit
) : ListAdapter<ActivityResponse, ActivityHistoryAdapter.ViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActivityHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = getItem(position)
        holder.bind(activity)
    }

    inner class ViewHolder(
        private val binding: ItemActivityHistoryBinding,
        private val onDeleteClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: ActivityResponse) {
            binding.tvActivityName.text = activity.displayName
            binding.tvDuration.text = "${activity.durationMinutes} mins"
            binding.tvDate.text = activity.createdAt

            activity.distanceKm?.let {
                binding.tvDistance.text = "$it km"
                binding.tvDistance.visibility = View.VISIBLE
            } ?: run {
                binding.tvDistance.visibility = View.GONE
            }

            activity.caloriesBurned?.let {
                binding.tvCalories.text = "$it cal"
                binding.tvCalories.visibility = View.VISIBLE
            } ?: run {
                binding.tvCalories.visibility = View.GONE
            }

            activity.notes?.takeIf { it.isNotEmpty() }?.let {
                binding.tvNotes.text = it
                binding.tvNotes.visibility = View.VISIBLE
            } ?: run {
                binding.tvNotes.visibility = View.GONE
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(activity.id)
            }

            // Set icon based on activity type
            val iconRes = when (activity.type) {
                "running" -> R.drawable.ic_running
                "cycling" -> R.drawable.ic_cycling
                "weightlifting" -> R.drawable.ic_weightlifting
                "swimming" -> R.drawable.ic_swimming
                "yoga" -> R.drawable.ic_yoga
                "walking" -> R.drawable.ic_walking
                else -> R.drawable.ic_other
            }
            binding.ivIcon.setImageResource(iconRes)
        }
    }
}

class ActivityDiffCallback : DiffUtil.ItemCallback<ActivityResponse>() {
    override fun areItemsTheSame(oldItem: ActivityResponse, newItem: ActivityResponse): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ActivityResponse, newItem: ActivityResponse): Boolean {
        return oldItem == newItem
    }
}
