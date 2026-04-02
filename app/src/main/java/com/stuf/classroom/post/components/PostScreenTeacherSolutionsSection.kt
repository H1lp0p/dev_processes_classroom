package com.stuf.classroom.post.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.stuf.classroom.post.PostUiState
import com.stuf.domain.model.SolutionId

@Composable
internal fun PostScreenTeacherSolutionsSection(
    state: PostUiState,
    onToggleCommentsVisibility: () -> Unit,
    onSolutionClick: (SolutionId) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onToggleCommentsVisibility,
            modifier = Modifier.testTag("post_teacher_toggle_comments_button"),
        ) {
            Text("Комментарии")
        }
    }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .testTag("post_solutions_list"),
    ) {
        items(state.solutions) { solution ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("post_solution_item")
                        .clickable { onSolutionClick(solution.id) },
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = solution.studentName)
                Text(text = solution.status)
            }
        }
    }
}
