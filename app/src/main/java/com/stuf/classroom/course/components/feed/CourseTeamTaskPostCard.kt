package com.stuf.classroom.course.components.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stuf.classroom.post.components.PostTaskScoreLine
import com.stuf.domain.model.TeamTaskPost

@Composable
internal fun CourseTeamTaskPostCard(
    post: TeamTaskPost,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CourseFeedPostCardShell(onClick = onClick, modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = null,
                )
//                Icon(
//                    imageVector = Icons.Outlined.Groups,
//                    contentDescription = null,
//                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Командное задание",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.bodyLarge,
                )
                PostTaskScoreLine(
                    assignedScore = post.assignedScore,
                    maxScore = post.taskDetails.maxScore,
                    compact = true,
                )
            }
        }
    }
}
