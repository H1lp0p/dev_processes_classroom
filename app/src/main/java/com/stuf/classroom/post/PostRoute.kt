package com.stuf.classroom.post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.stuf.domain.model.CommentId
import com.stuf.domain.model.SolutionId

@Composable
fun PostRoute(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
) {
    val viewModel: PostScreenViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onRetry()
    }

    PostScreen(
        state = state,
        onRetry = { viewModel.onRetry() },
        onAttachSolutionClick = {
            // временно можно показывать Toast на уровне Activity, здесь просто заглушка
        },
        onCommentSubmit = { text: String, isPrivate: Boolean, parent: CommentId? ->
            viewModel.onCommentSubmit(text, isPrivate, parent)
        },
        onLoadRepliesClick = { commentId: CommentId ->
            viewModel.onLoadRepliesClick(commentId)
        },
        onToggleCommentsVisibility = {
            viewModel.onToggleCommentsVisibility()
        },
        onSolutionClick = { solutionId: SolutionId ->
            // навигация к грейдингу будет добавлена позже
        },
        onBackClick = { navController.popBackStack() },
    )
}

