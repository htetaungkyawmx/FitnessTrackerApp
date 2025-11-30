package org.hak.fitnesstrackerapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.models.Exercise
import org.hak.fitnesstrackerapp.models.ExerciseCategory

class ExerciseAdapter(
    private val onExerciseClick: (Exercise) -> Unit = {},
    private val onExerciseLongClick: (Exercise) -> Unit = {},
    private val onSetComplete: (Exercise, Int) -> Unit = { _, _ -> },
    private val showProgress: Boolean = false
) : ListAdapter<Exercise, ExerciseAdapter.ExerciseViewHolder>(ExerciseDiffCallback) {

    private var isEditMode: Boolean = false

    fun setEditMode(editMode: Boolean) {
        isEditMode = editMode
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = getItem(position)
        holder.bind(exercise, isEditMode)
    }

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cardView: MaterialCardView = itemView.findViewById(R.id.exerciseCard)
        private val exerciseIcon: ImageView = itemView.findViewById(R.id.exerciseIcon)
        private val exerciseName: TextView = itemView.findViewById(R.id.exerciseName)
        private val exerciseDetails: TextView = itemView.findViewById(R.id.exerciseDetails)
        private val exerciseCategory: Chip = itemView.findViewById(R.id.exerciseCategory)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.exerciseProgressBar)
        private val progressText: TextView = itemView.findViewById(R.id.progressText)
        private val volumeText: TextView = itemView.findViewById(R.id.volumeText)
        private val oneRepMaxText: TextView = itemView.findViewById(R.id.oneRepMaxText)
        private val completedBadge: View = itemView.findViewById(R.id.completedBadge)
        private val setsContainer: ViewGroup? = itemView.findViewById(R.id.setsContainer)
        private val editIcon: ImageView = itemView.findViewById(R.id.editIcon)

        init {
            cardView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onExerciseClick(getItem(adapterPosition))
                }
            }

            cardView.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onExerciseLongClick(getItem(adapterPosition))
                    true
                } else {
                    false
                }
            }

            editIcon.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onExerciseLongClick(getItem(adapterPosition))
                }
            }
        }

        fun bind(exercise: Exercise, editMode: Boolean) {
            // Set basic exercise information
            exerciseName.text = exercise.name
            exerciseDetails.text = exercise.getFormattedDetails()
            exerciseCategory.text = exercise.category.getDisplayName()

            // Set category chip color
            exerciseCategory.setChipBackgroundColorResource(
                getCategoryColor(exercise.category)
            )

            // Set exercise icon
            exerciseIcon.setImageResource(getCategoryIcon(exercise.category))

            // Show/hide edit icon based on mode
            editIcon.visibility = if (editMode) View.VISIBLE else View.GONE

            // Show progress if enabled
            if (showProgress) {
                progressBar.visibility = View.VISIBLE
                progressText.visibility = View.VISIBLE
                volumeText.visibility = View.VISIBLE
                oneRepMaxText.visibility = View.VISIBLE

                val progress = exercise.getProgressPercentage().toInt()
                progressBar.progress = progress
                progressText.text = "${exercise.completedSets}/${exercise.sets} sets"

                // Set progress color based on completion
                val progressColor = when {
                    progress >= 100 -> R.color.success
                    progress >= 50 -> R.color.warning
                    else -> R.color.error
                }
                progressBar.progressTintList = ContextCompat.getColorStateList(
                    itemView.context, progressColor
                )

                // Show volume and one-rep max
                volumeText.text = "Volume: ${exercise.getTotalVolume().format(1)} kg"
                oneRepMaxText.text = "1RM: ${exercise.getOneRepMax().format(1)} kg"

                // FIXED: Use checkCompletion() instead of isCompleted()
                completedBadge.visibility = if (exercise.checkCompletion()) View.VISIBLE else View.GONE
            } else {
                progressBar.visibility = View.GONE
                progressText.visibility = View.GONE
                volumeText.visibility = View.GONE
                oneRepMaxText.visibility = View.GONE
                completedBadge.visibility = View.GONE
            }

            // Setup sets container for interactive sets
            setupSetsContainer(exercise)
        }

        private fun setupSetsContainer(exercise: Exercise) {
            // Only show sets container in workout mode
            if (showProgress && setsContainer != null) {
                setsContainer.visibility = View.VISIBLE

                // Clear existing set views
                setsContainer.removeAllViews()

                // Create set buttons
                for (setNumber in 1..exercise.sets) {
                    val setView = createSetView(setNumber, exercise, setNumber <= exercise.completedSets)
                    setsContainer.addView(setView)
                }
            } else {
                setsContainer?.visibility = View.GONE
            }
        }

        private fun createSetView(setNumber: Int, exercise: Exercise, isCompleted: Boolean): View {
            val setView = LayoutInflater.from(itemView.context)
                .inflate(R.layout.item_exercise_set, null)

            val setButton: MaterialCardView = setView.findViewById(R.id.setButton)
            val setNumberText: TextView = setView.findViewById(R.id.setNumberText)
            val setStatusIcon: ImageView = setView.findViewById(R.id.setStatusIcon)

            setNumberText.text = setNumber.toString()

            if (isCompleted) {
                setButton.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.success)
                )
                setNumberText.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.white)
                )
                setStatusIcon.setImageResource(R.drawable.ic_check)
                setStatusIcon.visibility = View.VISIBLE
            } else {
                setButton.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.surface)
                )
                setNumberText.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.on_surface)
                )
                setStatusIcon.visibility = View.GONE
            }

            setButton.setOnClickListener {
                if (!isCompleted) {
                    onSetComplete(exercise, setNumber)
                }
            }

            return setView
        }

        private fun getCategoryColor(category: ExerciseCategory): Int {
            return when (category) {
                ExerciseCategory.CHEST -> R.color.category_chest
                ExerciseCategory.BACK -> R.color.category_back
                ExerciseCategory.LEGS -> R.color.category_legs
                ExerciseCategory.SHOULDERS -> R.color.category_shoulders
                ExerciseCategory.ARMS -> R.color.category_arms
                ExerciseCategory.CORE -> R.color.category_core
                ExerciseCategory.CARDIO -> R.color.category_cardio
            }
        }

        private fun getCategoryIcon(category: ExerciseCategory): Int {
            return when (category) {
                ExerciseCategory.CHEST -> R.drawable.ic_chest
                ExerciseCategory.BACK -> R.drawable.ic_back
                ExerciseCategory.LEGS -> R.drawable.ic_legs
                ExerciseCategory.SHOULDERS -> R.drawable.ic_shoulders
                ExerciseCategory.ARMS -> R.drawable.ic_arms
                ExerciseCategory.CORE -> R.drawable.ic_core
                ExerciseCategory.CARDIO -> R.drawable.ic_cardio
            }
        }
    }

    object ExerciseDiffCallback : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem == newItem
        }
    }
}

// Extension function for Double formatting
private fun Double.format(decimalPlaces: Int): String {
    return String.format("%.${decimalPlaces}f", this)
}
