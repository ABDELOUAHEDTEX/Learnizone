package com.example.learnizone.models

import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

data class Enrollment(
    val enrollmentId: String = "",
    val userId: String = "",
    val courseId: String = "",
    val enrollmentDate: Date = Date(),
    val lastAccessDate: Date = Date(),
    val completionDate: Date? = null,
    val status: EnrollmentStatus = EnrollmentStatus.ACTIVE,
    val progress: Double = 0.0,
    val timeSpentMinutes: Int = 0,
    val lessonsCompleted: Int = 0,
    val totalLessons: Int = 0,
    val averageQuizScore: Double = 0.0,
    val certificateIssued: Boolean = false
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot): Enrollment {
            return Enrollment(
                enrollmentId = doc.id,
                userId = doc.getString("userId") ?: "",
                courseId = doc.getString("courseId") ?: "",
                enrollmentDate = doc.getDate("enrollmentDate") ?: Date(),
                lastAccessDate = doc.getDate("lastAccessDate") ?: Date(),
                completionDate = doc.getDate("completionDate"),
                status = EnrollmentStatus.fromString(doc.getString("status") ?: EnrollmentStatus.ACTIVE.value),
                progress = doc.getDouble("progress") ?: 0.0,
                timeSpentMinutes = doc.getLong("timeSpentMinutes")?.toInt() ?: 0,
                lessonsCompleted = doc.getLong("lessonsCompleted")?.toInt() ?: 0,
                totalLessons = doc.getLong("totalLessons")?.toInt() ?: 0,
                averageQuizScore = doc.getDouble("averageQuizScore") ?: 0.0,
                certificateIssued = doc.getBoolean("certificateIssued") ?: false
            )
        }
    }

    val isCompleted: Boolean
        get() = status == EnrollmentStatus.COMPLETED || progress >= 1.0

    val isActive: Boolean
        get() = status == EnrollmentStatus.ACTIVE

    val progressPercentage: String
        get() = "%.1f%%".format(progress * 100)

    val formattedTimeSpent: String
        get() {
            val hours = timeSpentMinutes / 60
            val minutes = timeSpentMinutes % 60
            return if (hours > 0) {
                "%dh %02dmin".format(hours, minutes)
            } else {
                "%dmin".format(minutes)
            }
        }

    val completionPercentage: Int
        get() = if (totalLessons == 0) 0 else (lessonsCompleted * 100) / totalLessons

    fun toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "courseId" to courseId,
        "enrollmentDate" to enrollmentDate,
        "lastAccessDate" to lastAccessDate,
        "completionDate" to (completionDate ?: FieldValue.delete()),
        "status" to status.value,
        "progress" to progress.coerceIn(0.0, 1.0),
        "timeSpentMinutes" to timeSpentMinutes,
        "lessonsCompleted" to lessonsCompleted,
        "totalLessons" to totalLessons,
        "averageQuizScore" to averageQuizScore,
        "certificateIssued" to certificateIssued
    )

    override fun toString(): String = buildString {
        append("Enrollment(")
        append("enrollmentId='$enrollmentId', ")
        append("userId='$userId', ")
        append("courseId='$courseId', ")
        append("status=$status, ")
        append("progress=$progress, ")
        append("lessonsCompleted=$lessonsCompleted, ")
        append("totalLessons=$totalLessons")
        append(")")
    }
}

enum class EnrollmentStatus(val value: String) {
    ACTIVE("active"),
    COMPLETED("completed"),
    SUSPENDED("suspended"),
    EXPIRED("expired"),
    CANCELLED("cancelled");

    companion object {
        fun fromString(value: String): EnrollmentStatus {
            return values().find { it.value == value } ?: ACTIVE
        }
    }
} 