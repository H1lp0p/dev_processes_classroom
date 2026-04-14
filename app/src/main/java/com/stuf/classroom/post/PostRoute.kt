package com.stuf.classroom.post

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.stuf.classroom.navigation.ClassroomRoutes
import com.stuf.domain.model.CommentId
import com.stuf.domain.model.TeamId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PostRoute(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
) {
    val viewModel: PostScreenViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                val name: String = uri.lastPathSegment ?: "attachment"
                withContext(Dispatchers.Main) {
                    viewModel.onPickedTeamSolutionFile(bytes, name)
                }
            }
        }

    LaunchedEffect(Unit) {
        viewModel.onRetry()
    }

    PostScreen(
        state = state,
        onRetry = { viewModel.onRetry() },
        onAttachSolutionClick = {
            // индивидуальное задание: отдельный поток выбора файла пока не подключён
        },
        onTeamTaskPickSolutionFile = {
            pickSolutionLauncher.launch("*/*")
        },
        onSubmitTeamSolution = { text -> viewModel.onSubmitTeamSolution(text) },
        onRemovePendingTeamSolutionFile = { id -> viewModel.onRemovePendingTeamSolutionFile(id) },
        onRemoveSavedTeamSolutionFile = { id -> viewModel.onRemoveSavedTeamSolutionFile(id) },
        onJoinTeam = { teamId: TeamId ->
            viewModel.onJoinTeam(teamId)
        },
        onLeaveTeam = { teamId: TeamId ->
            viewModel.onLeaveTeam(teamId)
        },
        onCommentSubmit = { text: String, isPrivate: Boolean, parent: CommentId? ->
            viewModel.onCommentSubmit(text, isPrivate, parent)
        },
        onLoadRepliesClick = { commentId: CommentId ->
            viewModel.onLoadRepliesClick(commentId)
        },
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
}
