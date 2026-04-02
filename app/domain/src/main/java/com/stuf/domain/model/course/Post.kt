package com.stuf.domain.model

import java.time.OffsetDateTime

data class Post(
    val id: PostId,
    val courseId: CourseId,
    val kind: PostKind,
    val title: String,
    val text: String,
    val createdAt: OffsetDateTime,
    val taskDetails: TaskDetails? = null,
)
