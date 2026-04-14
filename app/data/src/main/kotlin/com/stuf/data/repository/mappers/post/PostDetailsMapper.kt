package com.stuf.data.repository

import com.stuf.data.model.FileDto
import com.stuf.data.model.PostDetailsDto
import com.stuf.data.model.PostType
import com.stuf.data.model.TaskType
import com.stuf.domain.model.AnnouncementPost
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.MaterialPost
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostAttachment
import com.stuf.domain.model.PostId
import com.stuf.domain.model.Score
import com.stuf.domain.model.TaskDetails
import com.stuf.domain.model.TaskPost
import com.stuf.domain.model.TeamTaskPost
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Детали поста по API.
 *
 * **Материал:** в API нет отдельного значения типа; если `type == post` и список [PostDetailsDto.files]
 * непуст, в домене получается [MaterialPost], иначе [AnnouncementPost].
 *
 * В ответе нет [com.stuf.domain.model.Post.courseId]; подставляется [courseId] (по умолчанию — случайный UUID),
 * пока бэкенд не отдаёт идентификатор курса в DTO.
 */
internal fun mapPostDetailsDtoToPost(
    dto: PostDetailsDto,
    courseId: CourseId = CourseId(UUID.randomUUID()),
): Post {
    val createdAt: OffsetDateTime = dto.deadline ?: OffsetDateTime.now()
    val attachments: List<PostAttachment> = dto.files.orEmpty().mapNotNull { it.toPostAttachment() }

    return when (dto.type) {
        PostType.post ->
            if (attachments.isEmpty()) {
                AnnouncementPost(
                    id = PostId(dto.id!!),
                    courseId = courseId,
                    title = dto.title,
                    text = dto.text,
                    createdAt = createdAt,
                )
            } else {
                MaterialPost(
                    id = PostId(dto.id!!),
                    courseId = courseId,
                    title = dto.title,
                    text = dto.text,
                    createdAt = createdAt,
                    files = attachments,
                )
            }
        PostType.task -> {
            val taskDetails =
                TaskDetails(
                    deadline = dto.deadline,
                    isMandatory = dto.taskType == TaskType.mandatory,
                    maxScore = dto.maxScore ?: 5,
                )
            TaskPost(
                id = PostId(dto.id!!),
                courseId = courseId,
                title = dto.title,
                text = dto.text,
                createdAt = createdAt,
                taskDetails = taskDetails,
                attachments = attachments,
                assignedScore = dto.userSolution?.let { Score(it.score) },
            )
        }
        // OpenAPI: teaM_TASK → доменное командное задание ([TeamTaskPost]); см. [ApiPostTypeTeamTask].
        PostType.teaM_TASK -> {
            val taskDetails =
                TaskDetails(
                    deadline = dto.deadline,
                    isMandatory = dto.taskType == TaskType.mandatory,
                    maxScore = dto.maxScore ?: 5,
                )
            TeamTaskPost(
                id = PostId(dto.id!!),
                courseId = courseId,
                title = dto.title,
                text = dto.text,
                createdAt = createdAt,
                taskDetails = taskDetails,
                attachments = attachments,
                minTeamSize = dto.minTeamSize,
                maxTeamSize = dto.maxTeamSize,
                assignedScore = dto.teamSolution?.let { Score(it.score) },
            )
        }
    }
}

private fun FileDto.toPostAttachment(): PostAttachment? {
    val uuid: UUID? =
        id?.let { raw ->
            runCatching { UUID.fromString(raw) }.getOrNull()
        }
    return PostAttachment(id = uuid, name = name)
}
