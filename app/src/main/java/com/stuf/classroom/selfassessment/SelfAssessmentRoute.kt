package com.stuf.classroom.selfassessment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.stuf.classroom.post.PostScreenViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

@Composable
fun SelfAssessmentRoute(
    navController: NavController,
    @Suppress("UNUSED_PARAMETER") backStackEntry: NavBackStackEntry,
) {
    val viewModel: SelfAssessmentViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(PostScreenViewModel.REFRESH_TASK_SECTION_KEY, true)
            navController.popBackStack()
            viewModel.onSaveSuccessConsumed()
        }
    }

    SelfAssessmentScreen(
        state = state,
        onPop = { navController.popBackStack() },
        onWizardNavigateBack = viewModel::onWizardNavigateBack,
        onWizardNavigateNext = viewModel::onWizardNavigateNext,
        onWeightedScoreChange = viewModel::onWeightedScoreChange,
        onToggleChange = viewModel::onToggleChange,
        onSubmitSelfAssessment = viewModel::submitSelfAssessment,
        onDeleteSelfAssessment = viewModel::deleteMySelfAssessment,
    )
}
