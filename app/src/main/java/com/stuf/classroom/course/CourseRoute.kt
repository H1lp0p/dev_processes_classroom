package com.stuf.classroom.course

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.stuf.domain.model.PostId
import com.stuf.domain.model.UserId

@Composable
fun CourseRoute(
    viewModel: CourseScreenViewModel = hiltViewModel(),
    onPostClick: (PostId) -> Unit,
    onLeaveCourse: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onRetry()
    }

    CourseScreen(
        state = state,
        onTabSelected = viewModel::onTabSelected,
        onPostClick = { postId: PostId ->
            onPostClick(postId)
        },
        onMemberRoleToggleClick = { userId: UserId ->
            viewModel.onChangeMemberRoleClick(userId)
        },
        onMemberRemoveClick = { userId: UserId ->
            viewModel.onRemoveMemberClick(userId)
        },
        onBackClick = {
            onLeaveCourse()
        },
        onLeaveCourseClick = {
            viewModel.onLeaveCourseClick()
            onLeaveCourse()
        },
    )
}

