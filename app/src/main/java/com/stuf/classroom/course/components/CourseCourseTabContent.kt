package com.stuf.classroom.course.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.stuf.classroom.course.CourseScreenUiState
import com.stuf.classroom.course.components.feed.CourseFeedPostItem
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.CourseRole

@Composable
internal fun CourseCourseTabContent(
    state: CourseScreenUiState,
    onPostClick: (PostId) -> Unit,
    onHeaderClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("course_header_card")
                .clickable(onClick = onHeaderClick),
            colors = CardDefaults.cardColors(),
            elevation = CardDefaults.cardElevation(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.courseTitle,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.testTag("course_title"),
                )
                state.inviteCode?.let { code ->
                    Text(
                        text = "Код: $code",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag("course_invite_code"),
                    )
                }
                state.currentUserRole?.let { role ->
                    val roleLabel: String = when (role) {
                        CourseRole.TEACHER -> "Учитель"
                        CourseRole.STUDENT -> "Ученик"
                    }
                    Text(
                        text = roleLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag("course_user_role"),
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("course_posts_list"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.posts) { post: Post ->
                CourseFeedPostItem(
                    post = post,
                    onPostClick = onPostClick,
                )
            }
        }
    }
}
