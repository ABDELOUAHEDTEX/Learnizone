package com.example.learnizone.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.learnizone.databinding.ItemEnrolledCourseBinding
import com.example.learnizone.models.Enrollment
import com.example.learnizone.R

class MyCoursesAdapter(
    private val onCourseClick: (Enrollment) -> Unit,
    private val onContinueLearning: (Enrollment) -> Unit,
    private val onUnenroll: (Enrollment) -> Unit,
    private val onDownload: (Enrollment) -> Unit,
    private val onShare: (Enrollment) -> Unit
) : ListAdapter<Enrollment, MyCoursesAdapter.EnrolledCourseViewHolder>(EnrolledCourseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnrolledCourseViewHolder {
        val binding = ItemEnrolledCourseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EnrolledCourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EnrolledCourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EnrolledCourseViewHolder(
        private val binding: ItemEnrolledCourseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCourseClick(getItem(position))
                }
            }

            binding.buttonContinue.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onContinueLearning(getItem(position))
                }
            }

            binding.buttonUnenroll.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onUnenroll(getItem(position))
                }
            }

            binding.buttonDownload.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDownload(getItem(position))
                }
            }

            binding.buttonShare.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onShare(getItem(position))
                }
            }
        }

        fun bind(enrollment: Enrollment) {
            binding.apply {
                // TODO: Charger les détails du cours depuis le ViewModel
                textCourseTitle.text = "Chargement..."
                textCourseDescription.text = "Chargement..."
                
                textProgress.text = enrollment.progressPercentage
                progressBar.progress = (enrollment.progress * 100).toInt()
                
                textTimeSpent.text = enrollment.formattedTimeSpent
                textLessonsCompleted.text = "${enrollment.lessonsCompleted}/${enrollment.totalLessons}"
                
                // Afficher/masquer les boutons selon l'état
                buttonContinue.visibility = if (enrollment.isCompleted) ViewGroup.GONE else ViewGroup.VISIBLE
                buttonDownload.visibility = if (enrollment.isCompleted) ViewGroup.VISIBLE else ViewGroup.GONE
                
                // Mettre à jour le statut
                chipStatus.text = when {
                    enrollment.isCompleted -> "Terminé"
                    enrollment.progress > 0 -> "En cours"
                    else -> "Nouveau"
                }
                chipStatus.setChipBackgroundColorResource(
                    when {
                        enrollment.isCompleted -> R.color.status_completed
                        enrollment.progress > 0 -> R.color.status_in_progress
                        else -> R.color.status_new
                    }
                )
            }
        }
    }

    private class EnrolledCourseDiffCallback : DiffUtil.ItemCallback<Enrollment>() {
        override fun areItemsTheSame(oldItem: Enrollment, newItem: Enrollment): Boolean {
            return oldItem.enrollmentId == newItem.enrollmentId
        }

        override fun areContentsTheSame(oldItem: Enrollment, newItem: Enrollment): Boolean {
            return oldItem == newItem
        }
    }
} 