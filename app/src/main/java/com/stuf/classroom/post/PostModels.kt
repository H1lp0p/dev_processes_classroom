package com.stuf.classroom.post

import com.stuf.domain.model.CommentId
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.SolutionId

data class CommentUi(
    val id: String,
    val authorName: String,
    val text: String,
    val isPrivate: Boolean = false,
    val replies: List<CommentUi> = emptyList(),
)

data class SolutionUi(
    val id: SolutionId,
    val studentName: String,
    val status: String,
)

data class PostUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val postTitle: String = "",
    val postText: String = "",
    val isTask: Boolean = false,
    val currentUserRole: CourseRole = CourseRole.STUDENT,
    val comments: List<CommentUi> = emptyList(),
    val solutions: List<SolutionUi> = emptyList(),
    val areCommentsCollapsedForTeacher: Boolean = false,
)

