package com.stuf.classroom.courses

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseRole

@Composable
fun JoinCourseRoute(
    viewModel: JoinCourseViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToCourse: (CourseId, CourseRole) -> Unit,
) {
    val state: JoinCourseUiState by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isJoined, state.joinedCourseId) {
        val joinedCourseId: CourseId? = state.joinedCourseId
        if (state.isJoined && joinedCourseId != null) {
            // По умолчанию считаем, что присоединившийся пользователь студент.
            onNavigateToCourse(joinedCourseId, CourseRole.STUDENT)
        }
    }

    JoinCourseScreen(
        state = state,
        onInviteCodeChanged = viewModel::onInviteCodeChanged,
        onJoinClick = viewModel::onJoinClick,
        onBackClick = onBack,
    )
}

