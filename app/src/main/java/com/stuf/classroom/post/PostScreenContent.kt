package com.stuf.classroom.post

import com.stuf.domain.model.AnnouncementPost
import com.stuf.domain.model.MaterialPost
import com.stuf.domain.model.Post
import com.stuf.domain.model.TaskPost
import com.stuf.domain.model.TeamTaskPost

/** UI-слой экрана поста: одна ветка на тип доменного поста. */
sealed class PostScreenContent {
    data class Announcement(
        val post: AnnouncementPost,
    ) : PostScreenContent()

    data class Material(
        val post: MaterialPost,
    ) : PostScreenContent()

    data class Task(
        val post: TaskPost,
    ) : PostScreenContent()

    data class TeamTask(
        val post: TeamTaskPost,
    ) : PostScreenContent()
}

internal fun Post.toPostScreenContent(): PostScreenContent =
    when (this) {
        is AnnouncementPost -> PostScreenContent.Announcement(this)
        is MaterialPost -> PostScreenContent.Material(this)
        is TaskPost -> PostScreenContent.Task(this)
        is TeamTaskPost -> PostScreenContent.TeamTask(this)
    }
