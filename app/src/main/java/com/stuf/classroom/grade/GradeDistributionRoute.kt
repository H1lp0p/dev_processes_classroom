package com.stuf.classroom.grade

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

@Composable
fun GradeDistributionRoute(
    navController: NavController,
    @Suppress("UNUSED_PARAMETER") backStackEntry: NavBackStackEntry,
) {
    val viewModel: GradeDistributionViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    GradeDistributionScreen(
        state = state,
        onBack = { navController.popBackStack() },
        onDraftChange = viewModel::onDraftChange,
        onSave = viewModel::onSave,
        onVote = viewModel::onVote,
    )
}
