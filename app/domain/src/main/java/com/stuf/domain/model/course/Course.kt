package com.stuf.domain.model

data class Course(
    val id: CourseId,
    val title: String,
    val inviteCode: String?,
    val authorId: UserId? = null,
)
