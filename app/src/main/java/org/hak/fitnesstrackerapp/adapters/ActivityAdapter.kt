package org.hak.fitnesstrackerapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.models.FitnessActivity

class ActivityAdapter(
    private var activities: List<FitnessActivity>,
    private val onItemClick: (FitnessActivity) -> Unit
) : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    inner class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.ivActivityIcon)
        val tvType: TextView = itemView.findViewById(R.id.tvActivityType)
        val tvDuration: TextView = itemView.findViewById(R.id.tvActivityDuration)
        val tvDistance: TextView = itemView.findViewById(R.id.tvActivityDistance)
        val tvCalories: TextView = itemView.findViewById(R.id.tvActivityCalories)
        val tvDate: TextView = itemView.findViewById(R.id.tvActivityDate)

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(activities[adapterPosition])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]

        // Set icon based on activity type
        val iconRes = when (activity.type.lowercase()) {
            "running" -> R.drawable.ic_steps
            "cycling" -> R.drawable.ic_distance
            "walking" -> R.drawable.ic_duration
            "swimming" -> R.drawable.ic_calories
            "gym", "weightlifting" -> R.drawable.ic_fitness
            "yoga" -> R.drawable.ic_steps
            else -> R.drawable.ic_fitness
        }
        holder.ivIcon.setImageResource(iconRes)

        holder.tvType.text = activity.type
        holder.tvDuration.text = "${activity.duration} min"
        holder.tvDistance.text = String.format("%.1f km", activity.distance)
        holder.tvCalories.text = "${activity.calories} cal"
        holder.tvDate.text = activity.getShortDate() // Fixed here
    }

    override fun getItemCount(): Int = activities.size

    fun updateActivities(newActivities: List<FitnessActivity>) {
        this.activities = newActivities
        notifyDataSetChanged()
    }
}