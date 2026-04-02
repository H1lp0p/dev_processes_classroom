package com.stuf.classroom.post

import com.stuf.domain.model.CourseRole

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
