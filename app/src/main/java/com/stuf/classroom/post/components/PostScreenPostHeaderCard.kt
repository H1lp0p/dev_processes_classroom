package com.stuf.classroom.post.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stuf.classroom.post.PostUiState
import com.stuf.domain.model.CourseRole

@Composable
internal fun PostScreenPostHeaderCard(
    state: PostUiState,
    onAttachSolutionClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("post_header_card"),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = state.postTitle,
                modifier = Modifier.testTag("post_title"),
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state.postText,
                modifier = Modifier.testTag("post_text"),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (state.isTask) "Задание" else "Объявление",
                modifier = Modifier.testTag("post_type_label"),
            )

            if (state.isTask && state.currentUserRole == CourseRole.STUDENT) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAttachSolutionClick,
                    modifier = Modifier.testTag("post_attach_solution_button"),
                ) {
                    Text("Прикрепить решение")
                }
            }
        }
    }
}
