package com.example.learnizone.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnizone.managers.EnrollmentManager
import com.example.learnizone.models.Course
import com.example.learnizone.models.Enrollment
import kotlinx.coroutines.launch

class EnrollmentViewModel : ViewModel() {
    private val enrollmentManager = EnrollmentManager.getInstance()
    
    private val _enrollments = MutableLiveData<List<Enrollment>>()
    val enrollments: LiveData<List<Enrollment>> = _enrollments
    
    private val _courses = MutableLiveData<List<Course>>()
    val courses: LiveData<List<Course>> = _courses
    
    private val _enrollmentError = MutableLiveData<String>()
    val enrollmentError: LiveData<String> = _enrollmentError
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun enrollInCourse(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                enrollmentManager.enrollUserToCourse(courseId)
                loadUserEnrollments()
            } catch (e: Exception) {
                _enrollmentError.value = e.message ?: "Failed to enroll in course"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun unenrollFromCourse(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                enrollmentManager.unenrollUserFromCourse(courseId)
                loadUserEnrollments()
            } catch (e: Exception) {
                _enrollmentError.value = e.message ?: "Failed to unenroll from course"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserEnrollments() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val courses = enrollmentManager.getEnrolledCourses()
                _courses.value = courses
                
                val enrollments = enrollmentManager.getCoursesInProgress()
                _enrollments.value = enrollments
            } catch (e: Exception) {
                _enrollmentError.value = e.message ?: "Failed to load enrollments"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProgress(courseId: String, progress: Double) {
        viewModelScope.launch {
            try {
                enrollmentManager.updateCourseProgress(courseId, progress)
                loadUserEnrollments()
            } catch (e: Exception) {
                _enrollmentError.value = e.message ?: "Failed to update progress"
            }
        }
    }

    fun getCourseEnrollmentStats(courseId: String) {
        viewModelScope.launch {
            try {
                enrollmentManager.getCourseEnrollmentStats(courseId)
            } catch (e: Exception) {
                _enrollmentError.value = e.message ?: "Failed to get enrollment stats"
            }
        }
    }
} 