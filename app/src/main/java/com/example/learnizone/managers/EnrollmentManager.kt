package com.example.learnizone.managers

import android.util.Log
import com.example.learnizone.models.Course
import com.example.learnizone.models.Enrollment
import com.example.learnizone.models.EnrollmentStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

class EnrollmentManager private constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "EnrollmentManager"
        private const val ENROLLMENTS_COLLECTION = "enrollments"
        private const val COURSES_COLLECTION = "courses"
        private const val USERS_COLLECTION = "users"

        @Volatile
        private var instance: EnrollmentManager? = null

        fun getInstance(): EnrollmentManager {
            return instance ?: synchronized(this) {
                instance ?: EnrollmentManager().also { instance = it }
            }
        }
    }

    suspend fun enrollUserToCourse(courseId: String): Result<Boolean> = runCatching {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")

        // Vérifier si déjà inscrit
        if (checkIfAlreadyEnrolled(userId, courseId)) {
            throw Exception("Already enrolled")
        }

        // Créer l'inscription
        val enrollmentId = db.collection(ENROLLMENTS_COLLECTION).document().id
        val enrollment = Enrollment(
            enrollmentId = enrollmentId,
            userId = userId,
            courseId = courseId,
            enrollmentDate = Date(),
            status = EnrollmentStatus.ACTIVE,
            progress = 0.0
        )

        // Exécuter les opérations en batch
        db.runTransaction { transaction ->
            // Ajouter l'inscription
            transaction.set(
                db.collection(ENROLLMENTS_COLLECTION).document(enrollmentId),
                enrollment
            )

            // Mettre à jour le compteur d'étudiants
            transaction.update(
                db.collection(COURSES_COLLECTION).document(courseId),
                "enrolledStudents", FieldValue.increment(1)
            )

            // Ajouter le cours à la liste de l'utilisateur
            transaction.update(
                db.collection(USERS_COLLECTION).document(userId),
                "enrolledCourses", FieldValue.arrayUnion(courseId)
            )
        }.await()

        Log.d(TAG, "User enrolled successfully")
        true
    }

    suspend fun unenrollUserFromCourse(courseId: String): Result<Boolean> = runCatching {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")
        val enrollment = getEnrollment(userId, courseId) ?: throw Exception("Not enrolled")

        db.runTransaction { transaction ->
            // Supprimer l'inscription
            transaction.delete(
                db.collection(ENROLLMENTS_COLLECTION).document(enrollment.enrollmentId)
            )

            // Décrémenter le compteur d'étudiants
            transaction.update(
                db.collection(COURSES_COLLECTION).document(courseId),
                "enrolledStudents", FieldValue.increment(-1)
            )

            // Retirer le cours de la liste de l'utilisateur
            transaction.update(
                db.collection(USERS_COLLECTION).document(userId),
                "enrolledCourses", FieldValue.arrayRemove(courseId)
            )
        }.await()

        Log.d(TAG, "User unenrolled successfully")
        true
    }

    suspend fun checkIfAlreadyEnrolled(userId: String, courseId: String): Boolean {
        return db.collection(ENROLLMENTS_COLLECTION)
            .whereEqualTo("userId", userId)
            .whereEqualTo("courseId", courseId)
            .whereEqualTo("status", EnrollmentStatus.ACTIVE.name)
            .get()
            .await()
            .documents
            .isNotEmpty()
    }

    suspend fun getEnrollment(userId: String, courseId: String): Enrollment? {
        return db.collection(ENROLLMENTS_COLLECTION)
            .whereEqualTo("userId", userId)
            .whereEqualTo("courseId", courseId)
            .whereEqualTo("status", EnrollmentStatus.ACTIVE.name)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(Enrollment::class.java)
    }

    suspend fun getEnrolledCourses(): List<Course> {
        val userId = getCurrentUserId() ?: return emptyList()

        val enrollments = db.collection(ENROLLMENTS_COLLECTION)
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", EnrollmentStatus.ACTIVE.name)
            .get()
            .await()

        val courseIds = enrollments.documents.mapNotNull { it.getString("courseId") }
        if (courseIds.isEmpty()) return emptyList()

        return db.collection(COURSES_COLLECTION)
            .whereIn("id", courseIds)
            .get()
            .await()
            .documents
            .mapNotNull { Course.fromDocument(it) }
    }

    suspend fun getCoursesInProgress(): List<Enrollment> {
        val userId = getCurrentUserId() ?: return emptyList()

        return db.collection(ENROLLMENTS_COLLECTION)
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", EnrollmentStatus.ACTIVE.name)
            .whereGreaterThan("progress", 0.0)
            .whereLessThan("progress", 1.0)
            .orderBy("progress", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(Enrollment::class.java) }
    }

    suspend fun updateCourseProgress(courseId: String, progress: Double): Result<Unit> = runCatching {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")
        val enrollment = getEnrollment(userId, courseId) ?: throw Exception("Not enrolled")

        val updates = mutableMapOf<String, Any>(
            "progress" to progress.coerceIn(0.0, 1.0),
            "lastAccessDate" to Date()
        )

        // Si le cours est terminé
        if (progress >= 1.0) {
            updates["completionDate"] = Date()
            updates["status"] = EnrollmentStatus.COMPLETED.name
        }

        db.collection(ENROLLMENTS_COLLECTION)
            .document(enrollment.enrollmentId)
            .update(updates)
            .await()
    }

    suspend fun getCourseEnrollmentStats(courseId: String): Map<String, Any> {
        val snapshot = db.collection(ENROLLMENTS_COLLECTION)
            .whereEqualTo("courseId", courseId)
            .get()
            .await()

        val totalEnrollments = snapshot.size()
        var activeEnrollments = 0
        var completedEnrollments = 0
        var totalProgress = 0.0

        snapshot.documents.forEach { doc ->
            when (doc.getString("status")) {
                EnrollmentStatus.ACTIVE.name -> activeEnrollments++
                EnrollmentStatus.COMPLETED.name -> completedEnrollments++
            }
            doc.getDouble("progress")?.let { totalProgress += it }
        }

        val averageProgress = if (totalEnrollments > 0) totalProgress / totalEnrollments else 0.0
        val completionRate = if (totalEnrollments > 0) {
            (completedEnrollments.toDouble() / totalEnrollments) * 100
        } else 0.0

        return mapOf(
            "totalEnrollments" to totalEnrollments,
            "activeEnrollments" to activeEnrollments,
            "completedEnrollments" to completedEnrollments,
            "averageProgress" to averageProgress,
            "completionRate" to completionRate
        )
    }

    private fun getCurrentUserId(): String? = auth.currentUser?.uid
} 