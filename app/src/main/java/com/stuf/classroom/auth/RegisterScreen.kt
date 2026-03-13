package com.stuf.classroom.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onCredentialsChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRepeatPasswordChanged: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var repeatPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Регистрация",
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_title"),
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.credentials,
            onValueChange = onCredentialsChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_credentials_field"),
            label = { Text("Имя или ник") },
            singleLine = true,
        )

        if (!state.credentialsError.isNullOrEmpty()) {
            Text(
                text = state.credentialsError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_credentials_error"),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_email_field"),
            label = { Text("Email") },
            singleLine = true,
        )

        if (!state.emailError.isNullOrEmpty()) {
            Text(
                text = state.emailError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_email_error"),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_password_field"),
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                val icon = if (passwordVisible) {
                    Icons.Filled.VisibilityOff
                } else {
                    Icons.Filled.Visibility
                }
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = null)
                }
            },
        )

        if (!state.passwordError.isNullOrEmpty()) {
            Text(
                text = state.passwordError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_password_error"),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.repeatPassword,
            onValueChange = onRepeatPasswordChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_repeat_password_field"),
            label = { Text("Повторите пароль") },
            singleLine = true,
            visualTransformation = if (repeatPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                val icon = if (repeatPasswordVisible) {
                    Icons.Filled.VisibilityOff
                } else {
                    Icons.Filled.Visibility
                }
                IconButton(onClick = { repeatPasswordVisible = !repeatPasswordVisible }) {
                    Icon(imageVector = icon, contentDescription = null)
                }
            },
        )

        if (!state.repeatPasswordError.isNullOrEmpty()) {
            Text(
                text = state.repeatPasswordError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_repeat_password_error"),
            )
        }

        if (!state.generalError.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.generalError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_general_error"),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRegisterClick,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_button"),
        ) {
            Text(text = "Зарегистрироваться")
        }

        if (state.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(
                modifier = Modifier.testTag("register_loading_indicator"),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_to_login_button"),
        ) {
            Text(text = "У меня уже есть аккаунт")
        }
    }
}

