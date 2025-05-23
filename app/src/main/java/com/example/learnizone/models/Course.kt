package com.example.learnizone.models

import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

data class Course(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val instructorId: String = "",
    val instructorName: String = "",
    val thumbnailUrl: String = "",
    val enrolledStudents: Int = 0,
    val rating: Double = 0.0,
    val totalRatings: Int = 0,
    val price: Double = 0.0,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isPublished: Boolean = false,
    val category: String = "",
    val level: CourseLevel = CourseLevel.BEGINNER,
    val duration: Int = 0, // in minutes
    val prerequisites: List<String> = emptyList(),
    val tags: List<String> = emptyList()
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot): Course {
            return Course(
                id = doc.id,
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                instructorId = doc.getString("instructorId") ?: "",
                instructorName = doc.getString("instructorName") ?: "",
                thumbnailUrl = doc.getString("thumbnailUrl") ?: "",
                enrolledStudents = doc.getLong("enrolledStudents")?.toInt() ?: 0,
                rating = doc.getDouble("rating") ?: 0.0,
                totalRatings = doc.getLong("totalRatings")?.toInt() ?: 0,
                price = doc.getDouble("price") ?: 0.0,
                createdAt = doc.getDate("createdAt") ?: Date(),
                updatedAt = doc.getDate("updatedAt") ?: Date(),
                isPublished = doc.getBoolean("isPublished") ?: false,
                category = doc.getString("category") ?: "",
                level = CourseLevel.valueOf(doc.getString("level") ?: CourseLevel.BEGINNER.name),
                duration = doc.getLong("duration")?.toInt() ?: 0,
                prerequisites = (doc.get("prerequisites") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                tags = (doc.get("tags") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
        }
    }
}

enum class CourseLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
} 