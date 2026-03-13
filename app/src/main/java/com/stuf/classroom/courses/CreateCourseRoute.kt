package com.stuf.classroom.courses

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseRole

@Composable
fun CreateCourseRoute(
    viewModel: CreateCourseViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToCourse: (CourseId, CourseRole) -> Unit,
) {
    val state: CreateCourseUiState by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isCreated, state.createdCourseId) {
        val createdCourseId: CourseId? = state.createdCourseId
        if (state.isCreated && createdCourseId != null) {
            // Создатель курса всегда учитель.
            onNavigateToCourse(createdCourseId, CourseRole.TEACHER)
        }
    }

    CreateCourseScreen(
        state = state,
        onTitleChanged = viewModel::onTitleChanged,
        onCreateClick = viewModel::onCreateClick,
        onBackClick = onBack,
    )
}

