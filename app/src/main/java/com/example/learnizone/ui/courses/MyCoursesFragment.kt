package com.example.learnizone.ui.courses

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learnizone.R
import com.example.learnizone.adapters.MyCoursesAdapter
import com.example.learnizone.databinding.FragmentMyCoursesBinding
import com.example.learnizone.models.Enrollment
import com.example.learnizone.viewmodels.EnrollmentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout

class MyCoursesFragment : Fragment() {

    private var _binding: FragmentMyCoursesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: EnrollmentViewModel by viewModels()
    private lateinit var adapter: MyCoursesAdapter
    
    private val allEnrollments = mutableListOf<Enrollment>()
    private val inProgressEnrollments = mutableListOf<Enrollment>()
    private val completedEnrollments = mutableListOf<Enrollment>()
    
    companion object {
        private const val TAB_ALL = 0
        private const val TAB_IN_PROGRESS = 1
        private const val TAB_COMPLETED = 2
    }

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
        setupViews()
        setupObservers()
        loadCourses()
    }

    private fun setupViews() {
        setupToolbar()
        setupTabs()
        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupTabs() {
        binding.tabLayout.apply {
            addTab(newTab().setText("Tous"))
            addTab(newTab().setText("En cours"))
            addTab(newTab().setText("Terminés"))
            
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    filterCourses(tab.position)
                }
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
    }

    private fun setupRecyclerView() {
        adapter = MyCoursesAdapter(
            onCourseClick = { enrollment ->
                navigateToCourseDetail(enrollment.courseId)
            },
            onContinueLearning = { enrollment ->
                navigateToLesson(enrollment)
            },
            onUnenroll = { enrollment ->
                showUnenrollDialog(enrollment)
            },
            onDownload = { enrollment ->
                downloadCourseContent(enrollment)
            },
            onShare = { enrollment ->
                shareCourse(enrollment)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@MyCoursesFragment.adapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeResources(R.color.primary)
            setOnRefreshListener { loadCourses() }
        }
    }

    private fun setupFab() {
        binding.fabBrowseCourses.setOnClickListener {
            findNavController().navigate(R.id.action_myCoursesFragment_to_courseCatalogFragment)
        }
    }

    private fun setupObservers() {
        viewModel.enrollments.observe(viewLifecycleOwner) { enrollments ->
            updateEnrollments(enrollments)
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCourses() {
        showLoading(true)
        viewModel.loadUserEnrollments()
    }

    private fun updateEnrollments(enrollments: List<Enrollment>) {
        allEnrollments.clear()
        inProgressEnrollments.clear()
        completedEnrollments.clear()
        
        enrollments.forEach { enrollment ->
            allEnrollments.add(enrollment)
            when {
                enrollment.isCompleted -> completedEnrollments.add(enrollment)
                enrollment.progress > 0 -> inProgressEnrollments.add(enrollment)
            }
        }
        
        filterCourses(binding.tabLayout.selectedTabPosition)
        showLoading(false)
    }

    private fun filterCourses(tabPosition: Int) {
        val (filteredEnrollments, emptyMessage) = when (tabPosition) {
            TAB_IN_PROGRESS -> Pair(
                inProgressEnrollments,
                "Aucun cours en cours.\nCommencez un cours pour le voir ici !"
            )
            TAB_COMPLETED -> Pair(
                completedEnrollments,
                "Aucun cours terminé.\nTerminez un cours pour obtenir votre certificat !"
            )
            else -> Pair(
                allEnrollments,
                "Vous n'êtes inscrit à aucun cours.\nExplorez notre catalogue pour commencer !"
            )
        }
        
        adapter.submitList(filteredEnrollments)
        
        if (filteredEnrollments.isEmpty()) {
            showEmptyState(emptyMessage)
        } else {
            hideEmptyState()
        }
    }

    private fun showLoading(show: Boolean) {
        binding.apply {
            progressBar.visibility = if (show) View.VISIBLE else View.GONE
            recyclerView.visibility = if (show) View.GONE else View.VISIBLE
            emptyStateText.visibility = View.GONE
            swipeRefresh.isRefreshing = false
        }
    }

    private fun showEmptyState(message: String) {
        binding.apply {
            recyclerView.visibility = View.GONE
            emptyStateText.visibility = View.VISIBLE
            emptyStateText.text = message
        }
    }

    private fun hideEmptyState() {
        binding.apply {
            recyclerView.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
        }
    }

    private fun navigateToCourseDetail(courseId: String) {
        val action = MyCoursesFragmentDirections
            .actionMyCoursesFragmentToCourseDetailFragment(courseId)
        findNavController().navigate(action)
    }

    private fun navigateToLesson(enrollment: Enrollment) {
        val action = MyCoursesFragmentDirections
            .actionMyCoursesFragmentToLessonFragment(
                courseId = enrollment.courseId,
                enrollmentId = enrollment.enrollmentId
            )
        findNavController().navigate(action)
    }

    private fun showUnenrollDialog(enrollment: Enrollment) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Se désinscrire")
            .setMessage("Êtes-vous sûr de vouloir vous désinscrire de ce cours ? Votre progression sera perdue.")
            .setPositiveButton("Désinscrire") { _, _ ->
                unenrollFromCourse(enrollment)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun unenrollFromCourse(enrollment: Enrollment) {
        viewModel.unenrollFromCourse(enrollment.courseId)
    }

    private fun downloadCourseContent(enrollment: Enrollment) {
        // TODO: Implémenter le téléchargement pour utilisation hors ligne
        Toast.makeText(context, "Téléchargement disponible bientôt", Toast.LENGTH_SHORT).show()
    }

    private fun shareCourse(enrollment: Enrollment) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Découvrez ce cours sur LearnIzone")
            putExtra(Intent.EXTRA_TEXT, 
                "Je suis ce cours formidable sur LearnIzone ! Rejoignez-moi pour apprendre ensemble.")
        }
        
        startActivity(Intent.createChooser(shareIntent, "Partager le cours"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 