package com.stuf.classroom.peerreview

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.stuf.classroom.post.AttachmentDownloadUiEvent
import com.stuf.classroom.post.PostScreenViewModel

@Composable
fun PeerReviewRoute(
    navController: NavController,
    @Suppress("UNUSED_PARAMETER") backStackEntry: NavBackStackEntry,
) {
    val viewModel: PeerReviewViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.submitSuccess) {
        if (state.submitSuccess) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(PostScreenViewModel.REFRESH_TASK_SECTION_KEY, true)
            navController.popBackStack()
            viewModel.onSubmitSuccessConsumed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.attachmentDownloadEvents.collect { event ->
            when (event) {
                is AttachmentDownloadUiEvent.OpenUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                    try {
                        context.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            "Нет приложения для открытия файла",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
                is AttachmentDownloadUiEvent.Failure -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    PeerReviewScreen(
        state = state,
        wizardState = viewModel.wizardUiState(),
        onNavigateUp = { navController.popBackStack() },
        onWizardNavigateBack = viewModel::onWizardNavigateBack,
        onWizardNavigateNext = viewModel::onWizardNavigateNext,
        onWeightedScoreChange = viewModel::onWeightedScoreChange,
        onToggleChange = viewModel::onToggleChange,
        onSubmitReview = viewModel::submitReview,
        onTeamTargetSelected = viewModel::onTeamTargetSelected,
        onDownloadFile = viewModel::downloadAttachment,
    )
}
