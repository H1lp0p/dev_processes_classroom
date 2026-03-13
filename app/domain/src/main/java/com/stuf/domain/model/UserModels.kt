package com.stuf.domain.model

data class User(
    val id: UserId,
    val credentials: String,
    val email: String,
)

enum class CourseRole {
    TEACHER,
    STUDENT,
}

data class UserCourse(
    val id: CourseId,
    val title: String,
    val role: CourseRole,
)

data class CourseMember(
    val id: UserId,
    val credentials: String,
    val email: String,
    val role: CourseRole,
)

