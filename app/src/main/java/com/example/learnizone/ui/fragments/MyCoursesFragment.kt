package com.example.learnizone.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learnizone.databinding.FragmentMyCoursesBinding
import com.example.learnizone.viewmodels.EnrollmentViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyCoursesFragment : Fragment() {
    private var _binding: FragmentMyCoursesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: EnrollmentViewModel by viewModels()
    private lateinit var coursesAdapter: EnrolledCoursesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyCoursesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()
        observeViewModel()
        
        // Load initial data
        viewModel.loadUserEnrollments(getCurrentUserId())
    }

    private fun setupRecyclerView() {
        coursesAdapter = EnrolledCoursesAdapter(
            onCourseClick = { courseId ->
                navigateToCourseDetails(courseId)
            },
            onUnenrollClick = { enrollmentId ->
                viewModel.cancelEnrollment(enrollmentId, getCurrentUserId())
            }
        )
        
        binding.coursesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = coursesAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadUserEnrollments(getCurrentUserId())
        }
    }

    private fun setupFab() {
        binding.fabBrowseCourses.setOnClickListener {
            findNavController().navigate(
                MyCoursesFragmentDirections.actionMyCoursesToBrowseCourses()
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.enrollments.collectLatest { enrollments ->
                coursesAdapter.submitList(enrollments)
                updateEmptyState(enrollments.isEmpty())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.swipeRefresh.isRefreshing = isLoading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.enrollmentError.collectLatest { error ->
                error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.coursesRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun navigateToCourseDetails(courseId: String) {
        findNavController().navigate(
            MyCoursesFragmentDirections.actionMyCoursesToCourseDetails(courseId)
        )
    }

    private fun getCurrentUserId(): String {
        // TODO: Get current user ID from Firebase Auth
        return "current_user_id"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 