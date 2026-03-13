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
fun CreateCourseScreen(
    state: CreateCourseUiState,
    onTitleChanged: (String) -> Unit,
    onCreateClick: () -> Unit,
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
            text = "Новый курс",
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_course_title"),
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.title,
            onValueChange = onTitleChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_course_title_field"),
            label = { Text("Название курса") },
            singleLine = true,
        )

        if (!state.titleError.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.titleError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_course_title_error"),
            )
        }

        if (!state.generalError.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.generalError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_course_general_error"),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCreateClick,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_course_button"),
        ) {
            Text(text = "Создать")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onBackClick,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_course_back_button"),
        ) {
            Text(text = "Назад")
        }

        if (state.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(
                modifier = Modifier.testTag("create_course_loading_indicator"),
            )
        }
    }
}

