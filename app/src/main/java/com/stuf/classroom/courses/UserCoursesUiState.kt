package com.stuf.classroom.courses

import com.stuf.domain.model.UserCourse

data class UserCoursesUiState(
    val courses: List<UserCourse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
