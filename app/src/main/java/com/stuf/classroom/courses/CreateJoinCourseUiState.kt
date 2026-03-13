package com.stuf.classroom.courses

import com.stuf.domain.model.CourseId

data class CreateCourseUiState(
    val title: String = "",
    val isLoading: Boolean = false,
    val titleError: String? = null,
    val generalError: String? = null,
    val isCreated: Boolean = false,
    val createdCourseId: CourseId? = null,
)

data class JoinCourseUiState(
    val inviteCode: String = "",
    val isLoading: Boolean = false,
    val inviteCodeError: String? = null,
    val generalError: String? = null,
    val isJoined: Boolean = false,
    val joinedCourseId: CourseId? = null,
)

