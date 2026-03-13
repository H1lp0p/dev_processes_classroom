package com.stuf.classroom.courses

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.stuf.domain.model.UserCourse

@Composable
fun UserCoursesRoute(
    viewModel: UserCoursesViewModel,
    onLogout: () -> Unit,
    onNewCourse: () -> Unit,
    onJoinCourse: () -> Unit,
    onCourseClick: (UserCourse) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onRetry()
    }

    UserCoursesScreen(
        state = state,
        onRetry = viewModel::onRetry,
        onNewCourse = onNewCourse,
        onJoinCourse = onJoinCourse,
        onLogout = onLogout,
        onCourseClick = onCourseClick,
    )
}
