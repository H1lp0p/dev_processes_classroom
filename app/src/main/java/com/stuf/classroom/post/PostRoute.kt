package com.stuf.classroom.post

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.stuf.classroom.navigation.ClassroomRoutes
import com.stuf.domain.model.CommentId
import com.stuf.domain.model.TeamId
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.webkit.MimeTypeMap

@Composable
fun PostRoute(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
) {
    val viewModel: PostScreenViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val pickSolutionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri ->
            uri ?: return@rememberLauncherForActivityResult
            scope.launch(Dispatchers.IO) {
                val resolver = context.contentResolver
                val bytes =
                    resolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: return@launch
                val name: String = resolver.resolveUploadFileName(uri)
                withContext(Dispatchers.Main) {
                    viewModel.onPickedSolutionFile(bytes, name)
                }
            }
        }

    LaunchedEffect(Unit) {
        viewModel.onRetry()
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
                            "Нет приложения для открытия ссылки",
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

    LaunchedEffect(Unit) {
        viewModel.transientEvents.collect { event ->
            when (event) {
                is PostTransientUiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PostScreen(
            state = state,
            onRetry = { viewModel.onRetry() },
            onPickSolutionFile = {
                pickSolutionLauncher.launch("*/*")
            },
            onSubmitTeamSolution = { text -> viewModel.onSubmitTeamSolution(text) },
            onSubmitIndividualSolution = { text -> viewModel.onSubmitIndividualSolution(text) },
            onDeleteTeamSolution = { viewModel.onDeleteTeamSolution() },
            onDeleteIndividualSolution = { viewModel.onDeleteIndividualSolution() },
            onRemovePendingTeamSolutionFile = { id -> viewModel.onRemovePendingTeamSolutionFile(id) },
            onRemovePendingIndividualSolutionFile = { id ->
                viewModel.onRemovePendingIndividualSolutionFile(id)
            },
            onRemoveSavedTeamSolutionFile = { id -> viewModel.onRemoveSavedTeamSolutionFile(id) },
            onRemoveSavedIndividualSolutionFile = { id ->
                viewModel.onRemoveSavedIndividualSolutionFile(id)
            },
            onJoinTeam = { teamId: TeamId ->
                viewModel.onJoinTeam(teamId)
            },
            onLeaveTeam = { teamId: TeamId ->
                viewModel.onLeaveTeam(teamId)
            },
            onVoteCaptain = { teamId: TeamId, candidateId ->
                viewModel.onVoteCaptain(teamId, candidateId)
            },
            onTransferCaptain = { teamId: TeamId, toUserId ->
                viewModel.onTransferCaptain(teamId, toUserId)
            },
            onCommentSubmit = { text: String, isPrivate: Boolean, parent: CommentId? ->
                viewModel.onCommentSubmit(text, isPrivate, parent)
            },
            onEditComment = { commentId, text, isPrivate ->
                viewModel.onEditComment(commentId, text, isPrivate)
            },
            onDeleteComment = { commentId, isPrivate ->
                viewModel.onDeleteComment(commentId, isPrivate)
            },
            onLoadRepliesClick = { commentId: CommentId ->
                viewModel.onLoadRepliesClick(commentId)
            },
            onDownloadAttachment = { fileId: UUID -> viewModel.downloadAttachment(fileId) },
            onOpenGradeDistribution = {
                val st = viewModel.uiState.value
                val teamId = st.teamTask?.myTeam?.id ?: return@PostScreen
                val post =
                    (st.content as? PostScreenContent.TeamTask)?.post
                        ?: return@PostScreen
                navController.navigate(
                    ClassroomRoutes.gradeDistribution(
                        teamId = teamId,
                        postId = post.id,
                        role = st.currentUserRole,
                    ),
                )
            },
            onBackClick = { navController.popBackStack() },
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

private fun ContentResolver.resolveUploadFileName(uri: Uri): String {
    val displayName: String? =
        query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }

    val normalizedName: String = displayName?.takeIf { it.isNotBlank() } ?: (uri.lastPathSegment ?: "attachment")
    if (normalizedName.contains('.')) return normalizedName

    val extension: String? =
        getType(uri)
            ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
            ?.takeIf { it.isNotBlank() }

    return if (extension != null) "$normalizedName.$extension" else normalizedName
}
