package com.stuf.classroom.courses

import com.stuf.domain.model.CourseId

data class JoinCourseUiState(
    val inviteCode: String = "",
    val isLoading: Boolean = false,
    val inviteCodeError: String? = null,
    val generalError: String? = null,
    val isJoined: Boolean = false,
    val joinedCourseId: CourseId? = null,
)
