package com.example.learnizone.repositories

import com.example.learnizone.models.Enrollment
import com.example.learnizone.models.EnrollmentStatus
import com.example.learnizone.models.UserCourses
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class EnrollmentRepository(private val coroutineScope: CoroutineScope) {
    private val db = FirebaseFirestore.getInstance()
    private val enrollmentsCollection = db.collection("enrollments")
    private val userCoursesCollection = db.collection("user_courses")

    suspend fun enrollInCourse(userId: String, courseId: String): Result<Enrollment> = suspendCoroutine { continuation ->
        val enrollment = Enrollment(
            userId = userId,
            courseId = courseId,
            status = EnrollmentStatus.ACTIVE
        )

        enrollmentsCollection.add(enrollment)
            .addOnSuccessListener { documentReference ->
                val newEnrollment = enrollment.copy(enrollmentId = documentReference.id)
                coroutineScope.launch {
                    updateUserCourses(userId, courseId, true)
                    continuation.resume(Result.success(newEnrollment))
                }
            }
            .addOnFailureListener { e ->
                continuation.resume(Result.failure(e))
            }
    }

    suspend fun cancelEnrollment(enrollmentId: String): Result<Unit> = suspendCoroutine { continuation ->
        enrollmentsCollection.document(enrollmentId)
            .update("status", EnrollmentStatus.CANCELLED.value)
            .addOnSuccessListener {
                coroutineScope.launch {
                    continuation.resume(Result.success(Unit))
                }
            }
            .addOnFailureListener { e ->
                continuation.resume(Result.failure(e))
            }
    }

    suspend fun getUserEnrollments(userId: String): Result<List<Enrollment>> = suspendCoroutine { continuation ->
        enrollmentsCollection
            .whereEqualTo("userId", userId)
            .whereNotEqualTo("status", EnrollmentStatus.CANCELLED.value)
            .get()
            .addOnSuccessListener { documents ->
                val enrollments = documents.mapNotNull { it.toObject(Enrollment::class.java) }
                continuation.resume(Result.success(enrollments))
            }
            .addOnFailureListener { e ->
                continuation.resume(Result.failure(e))
            }
    }

    private suspend fun updateUserCourses(userId: String, courseId: String, isEnrolling: Boolean) {
        val userCoursesRef = userCoursesCollection.document(userId)
        
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userCoursesRef)
            val userCourses = snapshot.toObject(UserCourses::class.java) ?: UserCourses(userId = userId, enrolledCourses = emptyList())
            
            val updatedEnrolledCourses = if (isEnrolling) {
                userCourses.enrolledCourses + courseId
            } else {
                userCourses.enrolledCourses - courseId
            }
            
            transaction.set(userCoursesRef, userCourses.copy(enrolledCourses = updatedEnrolledCourses))
        }.await()
    }

    suspend fun updateEnrollmentProgress(enrollmentId: String, progress: Double): Result<Unit> = suspendCoroutine { continuation ->
        enrollmentsCollection.document(enrollmentId)
            .update("progress", progress.coerceIn(0.0, 1.0))
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                continuation.resume(Result.failure(e))
            }
    }
} 