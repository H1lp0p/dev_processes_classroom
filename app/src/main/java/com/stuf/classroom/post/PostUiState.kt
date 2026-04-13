package com.stuf.classroom.post

import com.stuf.domain.model.CourseRole

data class PostUiState(
    val isLoadingPost: Boolean = false,
    val postLoadError: String? = null,
    val isLoadingComments: Boolean = false,
    val commentsLoadError: String? = null,
    /** Задел под загрузку данных команд с отдельных эндпоинтов (пока не используется). */
    val isLoadingTeamSection: Boolean = false,
    val teamSectionError: String? = null,
    /** Подгрузка ответов для комментария (id комментария). */
    val loadingRepliesForCommentId: String? = null,
    val content: PostScreenContent? = null,
    val comments: List<CommentUi> = emptyList(),
    val currentUserRole: CourseRole = CourseRole.STUDENT,
)
