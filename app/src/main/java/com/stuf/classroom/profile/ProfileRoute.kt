package com.stuf.classroom.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProfileRoute(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onRetry()
    }

    ProfileScreen(
        state = state,
        onRetry = viewModel::onRetry,
        onBack = onBack,
        onLogout = onLogout,
        onEditProfile = onEditProfile,
        onChangePassword = onChangePassword,
    )
}
