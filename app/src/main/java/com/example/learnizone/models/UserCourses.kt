package com.example.learnizone.models

data class UserCourses(
    val userId: String = "",
    val enrolledCourses: List<String> = emptyList()
) 