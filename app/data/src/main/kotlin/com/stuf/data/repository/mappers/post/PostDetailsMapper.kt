package com.stuf.data.repository

import com.stuf.data.model.PostDetailsDto
import com.stuf.data.model.PostType
import com.stuf.data.model.TaskType
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.PostKind
import com.stuf.domain.model.TaskDetails
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Детали поста по API. В ответе нет courseId; в доменной модели [Post.courseId] подставляется
 * [courseId] (по умолчанию — случайный UUID), пока бэкенд не отдаёт идентификатор курса в DTO.
 */
internal fun mapPostDetailsDtoToPost(
    dto: PostDetailsDto,
    courseId: CourseId = CourseId(UUID.randomUUID()),
): Post {
    val kind = when (dto.type) {
        PostType.post -> PostKind.ANNOUNCEMENT
        PostType.task -> PostKind.TASK
    }

    val taskDetails = if (dto.type == PostType.task) {
        TaskDetails(
            deadline = dto.deadline,
            isMandatory = dto.taskType == TaskType.mandatory,
            maxScore = dto.maxScore ?: 5,
        )
    } else {
        null
    }

    return Post(
        id = PostId(dto.id!!),
        courseId = courseId,
        kind = kind,
        title = dto.title,
        text = dto.text,
        createdAt = dto.deadline ?: OffsetDateTime.now(),
        taskDetails = taskDetails,
    )
}
