package com.example.learnizone.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.learnizone.databinding.ItemEnrolledCourseBinding
import com.example.learnizone.models.Enrollment

class EnrolledCoursesAdapter(
    private val onCourseClick: (String) -> Unit,
    private val onUnenrollClick: (String) -> Unit
) : ListAdapter<Enrollment, EnrolledCoursesAdapter.EnrolledCourseViewHolder>(EnrolledCourseDiffCallback()) {

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
                    onCourseClick(getItem(position).courseId)
                }
            }

            binding.buttonUnenroll.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onUnenrollClick(getItem(position).enrollmentId)
                }
            }
        }

        fun bind(enrollment: Enrollment) {
            binding.apply {
                // TODO: Load course details from Firestore
                textCourseTitle.text = "Course ${enrollment.courseId}"
                textProgress.text = "${(enrollment.progress * 100).toInt()}%"
                progressBar.progress = (enrollment.progress * 100).toInt()
                chipStatus.text = enrollment.status.name
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