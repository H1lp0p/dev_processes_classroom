package com.stuf.domain.model

import java.time.OffsetDateTime
import java.util.UUID

/** Вложение к посту (идентификатор и имя с бэкенда). */
data class PostAttachment(
    val id: UUID?,
    val name: String?,
)

/**
 * Пост курса: варианты различаются структурой и поведением.
 *
 * **Материал** на бэкенде не выделен отдельным `PostType`: доменный [MaterialPost] задаётся
 * сочетанием `type == post` и непустого списка `files` в DTO деталей поста.
 */
sealed class Post {
    abstract val id: PostId
    abstract val courseId: CourseId
    abstract val title: String
    abstract val text: String
    abstract val createdAt: OffsetDateTime
}

data class AnnouncementPost(
    override val id: PostId,
    override val courseId: CourseId,
    override val title: String,
    override val text: String,
    override val createdAt: OffsetDateTime,
) : Post()

data class MaterialPost(
    override val id: PostId,
    override val courseId: CourseId,
    override val title: String,
    override val text: String,
    override val createdAt: OffsetDateTime,
    val files: List<PostAttachment>,
) : Post()

data class TaskPost(
    override val id: PostId,
    override val courseId: CourseId,
    override val title: String,
    override val text: String,
    override val createdAt: OffsetDateTime,
    val taskDetails: TaskDetails,
    val attachments: List<PostAttachment> = emptyList(),
) : Post()

data class TeamTaskPost(
    override val id: PostId,
    override val courseId: CourseId,
    override val title: String,
    override val text: String,
    override val createdAt: OffsetDateTime,
    val taskDetails: TaskDetails,
    val attachments: List<PostAttachment> = emptyList(),
) : Post()

fun Post.typeLabelForScreen(): String =
    when (this) {
        is AnnouncementPost -> "Пост"
        is MaterialPost -> "Материал"
        is TaskPost -> "Задание"
        is TeamTaskPost -> "Командное задание"
    }

/** Индивидуальное или командное задание (есть решение/дедлайн с точки зрения домена). */
fun Post.isTask(): Boolean = this is TaskPost || this is TeamTaskPost
