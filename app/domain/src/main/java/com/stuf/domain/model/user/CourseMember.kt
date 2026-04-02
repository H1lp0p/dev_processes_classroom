package com.stuf.domain.model

data class CourseMember(
    val id: UserId,
    val credentials: String,
    val email: String,
    val role: CourseRole,
)
