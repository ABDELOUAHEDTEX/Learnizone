package com.example.learnizone.repositories

import com.example.learnizone.models.Enrollment
import com.example.learnizone.models.EnrollmentStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class EnrollmentRepository {
    private val db = FirebaseFirestore.getInstance()
    private val enrollmentsCollection = db.collection("enrollments")
    private val userCoursesCollection = db.collection("user_courses")

    suspend fun enrollInCourse(userId: String, courseId: String): Result<Enrollment> = suspendCoroutine { continuation ->
        val enrollment = Enrollment(
            userId = userId,
            courseId = courseId,
            status = EnrollmentStatus.PENDING
        )

        enrollmentsCollection.add(enrollment)
            .addOnSuccessListener { documentReference ->
                val newEnrollment = enrollment.copy(enrollmentId = documentReference.id)
                updateUserCourses(userId, courseId, true)
                continuation.resume(Result.success(newEnrollment))
            }
            .addOnFailureListener { e ->
                continuation.resume(Result.failure(e))
            }
    }

    suspend fun cancelEnrollment(enrollmentId: String): Result<Unit> = suspendCoroutine { continuation ->
        enrollmentsCollection.document(enrollmentId)
            .update("status", EnrollmentStatus.CANCELLED)
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                continuation.resume(Result.failure(e))
            }
    }

    suspend fun getUserEnrollments(userId: String): Result<List<Enrollment>> = suspendCoroutine { continuation ->
        enrollmentsCollection
            .whereEqualTo("userId", userId)
            .whereNotEqualTo("status", EnrollmentStatus.CANCELLED)
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
            val userCourses = snapshot.toObject(UserCourses::class.java) ?: UserCourses(userId = userId)
            
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
            .update("progress", progress)
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                continuation.resume(Result.failure(e))
            }
    }
} 