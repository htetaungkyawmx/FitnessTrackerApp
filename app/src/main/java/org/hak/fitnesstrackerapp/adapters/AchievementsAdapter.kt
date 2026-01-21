package org.hak.fitnesstrackerapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.model.Achievement

class AchievementsAdapter(private val achievements: List<Achievement>) :
    RecyclerView.Adapter<AchievementsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardAchievement)
        val ivIcon: ImageView = itemView.findViewById(R.id.ivAchievementIcon)
        val tvTitle: TextView = itemView.findViewById(R.id.tvAchievementTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvAchievementDescription)
        val tvPoints: TextView = itemView.findViewById(R.id.tvAchievementPoints)
        val tvStatus: TextView = itemView.findViewById(R.id.tvAchievementStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val achievement = achievements[position]

        holder.ivIcon.setImageResource(achievement.iconRes)
        holder.tvTitle.text = achievement.title
        holder.tvDescription.text = achievement.description
        holder.tvPoints.text = "+${achievement.points} pts"

        if (achievement.unlocked) {
            holder.tvStatus.text = "UNLOCKED"
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
            holder.ivIcon.alpha = 1.0f
        } else {
            holder.tvStatus.text = "LOCKED"
            holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E"))
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.background_secondary)
            )
            holder.ivIcon.alpha = 0.3f
        }
    }

    override fun getItemCount(): Int = achievements.size
}