package com.stuf.classroom.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Простой debug-экран для авторизованного пользователя.
 *
 * Показывает текущее состояние авторизации и содержимое AuthSession,
 * чтобы можно было визуально убедиться в работе login/register/refresh.
 */
@Composable
fun HomeDebugScreen(
    state: AuthState,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = "Home / Debug",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AuthState:",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = when (state) {
                is AuthState.Initial -> "Initial"
                is AuthState.Unauthenticated -> "Unauthenticated"
                is AuthState.Loading -> "Loading"
                is AuthState.Authenticated -> "Authenticated"
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        if (state is AuthState.Authenticated) {
            Text(
                text = "AuthSession:",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "accessToken: ${state.session.accessToken}",
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "refreshToken: ${state.session.refreshToken}",
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text(
                text = "Нет активной AuthSession",
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Выйти (logout)")
        }
    }
}

