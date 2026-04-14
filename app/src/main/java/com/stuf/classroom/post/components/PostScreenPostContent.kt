package com.stuf.classroom.post.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stuf.classroom.courses.components.UserCourseStyleCard
import com.stuf.domain.model.AnnouncementPost
import com.stuf.domain.model.MaterialPost
import com.stuf.domain.model.PostAttachment
import com.stuf.domain.model.TaskPost
import com.stuf.domain.model.TeamTaskPost
import com.stuf.domain.model.typeLabelForScreen

@Composable
internal fun PostScreenAnnouncementSection(
    post: AnnouncementPost,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PostHeaderUserCourseCard(
            title = post.title,
            typeLabel = post.typeLabelForScreen(),
            icon = Icons.Outlined.Campaign,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = post.text,
            modifier = Modifier.testTag("post_text"),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
internal fun PostScreenMaterialSection(
    post: MaterialPost,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PostHeaderUserCourseCard(
            title = post.title,
            typeLabel = post.typeLabelForScreen(),
            icon = Icons.Outlined.MenuBook,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = post.text,
            modifier = Modifier.testTag("post_text"),
            style = MaterialTheme.typography.bodyLarge,
        )
        if (post.files.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            post.files.forEach { attachment ->
                PostFileAttachmentCard(attachment = attachment)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
internal fun PostScreenTaskSection(
    post: TaskPost,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PostHeaderUserCourseCard(
            title = post.title,
            typeLabel = post.typeLabelForScreen(),
            icon = Icons.Outlined.Assignment,
        )
        Spacer(modifier = Modifier.height(8.dp))
        PostTaskScoreLine(
            assignedScore = post.assignedScore,
            maxScore = post.taskDetails.maxScore,
            compact = false,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = post.text,
            modifier = Modifier.testTag("post_text"),
            style = MaterialTheme.typography.bodyLarge,
        )
        if (post.attachments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            post.attachments.forEach { attachment ->
                PostFileAttachmentCard(attachment = attachment)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/** Минимальная разметка: без отдельной полировки командного UI. */
@Composable
internal fun PostScreenTeamTaskSection(
    post: TeamTaskPost,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PostHeaderUserCourseCard(
            title = post.title,
            typeLabel = post.typeLabelForScreen(),
            icon = Icons.Outlined.Groups,
        )
        Spacer(modifier = Modifier.height(8.dp))
        PostTaskScoreLine(
            assignedScore = post.assignedScore,
            maxScore = post.taskDetails.maxScore,
            compact = false,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = post.text,
            modifier = Modifier.testTag("post_text"),
            style = MaterialTheme.typography.bodyLarge,
        )
        if (post.attachments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            post.attachments.forEach { attachment ->
                PostFileAttachmentCard(attachment = attachment)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
internal fun PostScreenTeamTaskRulesBlock(
    post: TeamTaskPost,
    modifier: Modifier = Modifier,
) {
    val label: String? =
        when {
            post.minTeamSize != null && post.maxTeamSize != null ->
                "Состав команды: от ${post.minTeamSize} до ${post.maxTeamSize} человек"
            post.minTeamSize != null -> "Минимум участников в команде: ${post.minTeamSize}"
            post.maxTeamSize != null -> "Максимум участников в команде: ${post.maxTeamSize}"
            else -> null
        }
    if (label == null) return
    Text(
        text = label,
        modifier = modifier.testTag("team_task_rules"),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun PostHeaderUserCourseCard(
    title: String,
    typeLabel: String,
    icon: ImageVector,
) {
    UserCourseStyleCard(
        title = title,
        subtitle = typeLabel,
        icon = icon,
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("post_header_card"),
        titleModifier = Modifier.testTag("post_title"),
        subtitleModifier = Modifier.testTag("post_type_label"),
    )
}

@Composable
internal fun PostFileAttachmentCard(
    attachment: PostAttachment,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("post_file_card"),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
        ) {
            Text(
                text = attachment.name ?: (attachment.id?.toString() ?: "Файл"),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Вложение",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
internal fun PostScreenCommentsDivider(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
    }
}
