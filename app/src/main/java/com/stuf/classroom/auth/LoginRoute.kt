package com.stuf.classroom.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun LoginRoute(
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit,
    onAuthSuccess: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            onAuthSuccess()
        }
    }

    LoginScreen(
        state = state,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onLoginClick = viewModel::onLoginClick,
        onNavigateToRegister = onNavigateToRegister,
    )
}

