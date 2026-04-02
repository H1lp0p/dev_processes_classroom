package com.stuf.data.repository

import com.stuf.data.model.CourseFeedItemDto
import com.stuf.data.model.PostType
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.PostKind

/**
 * Лента курса: [CourseFeedItemDto] содержит только id, type, title, createdDate (контракт OpenAPI).
 * Текст поста и поля задания отсутствуют — в домене остаются пустой текст и null [TaskDetails].
 * Полные данные доступны только после вызова [com.stuf.domain.repository.PostRepository.getPost].
 */
internal fun mapCourseFeedItemToPost(
    item: CourseFeedItemDto,
    courseId: CourseId,
): Post =
    Post(
        id = PostId(item.id),
        courseId = courseId,
        kind = when (item.type) {
            PostType.post -> PostKind.ANNOUNCEMENT
            PostType.task -> PostKind.TASK
        },
        title = item.title,
        text = "",
        createdAt = item.createdDate,
        taskDetails = null,
    )
