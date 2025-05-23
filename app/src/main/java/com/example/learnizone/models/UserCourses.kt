package com.example.learnizone.models

data class UserCourses(
    val userId: String = "",
    val enrolledCourses: List<String> = emptyList(),
    val completedCourses: List<String> = emptyList(),
    val wishlistCourses: List<String> = emptyList()
) 