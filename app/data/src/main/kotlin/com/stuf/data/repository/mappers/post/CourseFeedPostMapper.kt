package com.stuf.data.repository

import com.stuf.data.model.CourseFeedItemDto
import com.stuf.data.model.PostType
import com.stuf.domain.model.AnnouncementPost
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.TaskDetails
import com.stuf.domain.model.TaskPost
import com.stuf.domain.model.TeamTaskPost

/**
 * Лента курса: [CourseFeedItemDto] содержит только id, type, title, createdDate (контракт OpenAPI).
 * Текст поста и поля задания отсутствуют — в домене остаются пустой текст и упрощённые [TaskDetails] для заданий.
 * Полные данные и классификация «материал по files» доступны только после [com.stuf.domain.repository.PostRepository.getPost].
 */
internal fun mapCourseFeedItemToPost(
    item: CourseFeedItemDto,
    courseId: CourseId,
): Post {
    val placeholderDetails =
        TaskDetails(
            deadline = null,
            isMandatory = true,
            maxScore = 5,
        )
    return when (item.type) {
        PostType.post ->
            AnnouncementPost(
                id = PostId(item.id),
                courseId = courseId,
                title = item.title,
                text = "",
                createdAt = item.createdDate,
            )
        PostType.task ->
            TaskPost(
                id = PostId(item.id),
                courseId = courseId,
                title = item.title,
                text = "",
                createdAt = item.createdDate,
                taskDetails = placeholderDetails,
                attachments = emptyList(),
            )
        // OpenAPI: teaM_TASK → доменное командное задание ([TeamTaskPost]); см. [ApiPostTypeTeamTask].
        PostType.teaM_TASK ->
            TeamTaskPost(
                id = PostId(item.id),
                courseId = courseId,
                title = item.title,
                text = "",
                createdAt = item.createdDate,
                taskDetails = placeholderDetails,
                attachments = emptyList(),
            )
    }
}
