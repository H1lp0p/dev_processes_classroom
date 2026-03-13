package com.stuf.classroom.courses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun JoinCourseScreen(
    state: JoinCourseUiState,
    onInviteCodeChanged: (String) -> Unit,
    onJoinClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Присоединиться к курсу",
            modifier = Modifier
                .fillMaxWidth()
                .testTag("join_course_title"),
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.inviteCode,
            onValueChange = onInviteCodeChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("join_course_code_field"),
            label = { Text("Код приглашения") },
            singleLine = true,
        )

        if (!state.inviteCodeError.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.inviteCodeError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("join_course_code_error"),
            )
        }

        if (!state.generalError.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.generalError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("join_course_general_error"),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onJoinClick,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("join_course_button"),
        ) {
            Text(text = "Присоединиться")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onBackClick,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("join_course_back_button"),
        ) {
            Text(text = "Назад")
        }

        if (state.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(
                modifier = Modifier.testTag("join_course_loading_indicator"),
            )
        }
    }
}

