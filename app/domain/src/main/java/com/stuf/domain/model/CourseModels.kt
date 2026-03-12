package com.stuf.domain.model

import java.time.OffsetDateTime

data class Course(
    val id: CourseId,
    val title: String,
    val inviteCode: String?,
)

enum class PostKind {
    ANNOUNCEMENT,
    MATERIAL,
    TASK,
}

data class TaskDetails(
    val deadline: OffsetDateTime?,
    val isMandatory: Boolean,
    val maxScore: Int = 5,
) {
    init {
        require(maxScore > 0) { "maxScore must be positive" }
    }
}

data class Post(
    val id: PostId,
    val courseId: CourseId,
    val kind: PostKind,
    val title: String,
    val text: String,
    val createdAt: OffsetDateTime,
    val taskDetails: TaskDetails? = null,
)

