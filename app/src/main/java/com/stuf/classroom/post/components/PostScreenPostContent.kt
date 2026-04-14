package com.stuf.classroom.post.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

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
        Spacer(modifier = Modifier.height(8.dp))
        PostHeaderDetailsCollapsible(
            createdAt = post.createdAt,
            deadline = null,
            teamCompositionLabel = null,
            maxScore = null,
        )
        Spacer(modifier = Modifier.height(16.dp))
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
    onAttachmentDownload: (fileId: UUID) -> Unit = {},
) {
    Column(modifier = modifier) {
        PostHeaderUserCourseCard(
            title = post.title,
            typeLabel = post.typeLabelForScreen(),
            icon = Icons.Outlined.MenuBook,
        )
        Spacer(modifier = Modifier.height(8.dp))
        PostHeaderDetailsCollapsible(
            createdAt = post.createdAt,
            deadline = null,
            teamCompositionLabel = null,
            maxScore = null,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = post.text,
            modifier = Modifier.testTag("post_text"),
            style = MaterialTheme.typography.bodyLarge,
        )
        if (post.files.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            post.files.forEach { attachment ->
                PostFileAttachmentCard(
                    attachment = attachment,
                    onClick =
                        attachment.id?.let { id ->
                            { onAttachmentDownload(id) }
                        },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
internal fun PostScreenTaskSection(
    post: TaskPost,
    modifier: Modifier = Modifier,
    onAttachmentDownload: (fileId: UUID) -> Unit = {},
) {
    Column(modifier = modifier) {
        PostHeaderUserCourseCard(
            title = post.title,
            typeLabel = post.typeLabelForScreen(),
            icon = Icons.Outlined.Assignment,
        )
        Spacer(modifier = Modifier.height(8.dp))
        PostHeaderDetailsCollapsible(
            createdAt = post.createdAt,
            deadline = post.taskDetails.deadline,
            teamCompositionLabel = null,
            maxScore = post.taskDetails.maxScore,
        )
        Spacer(modifier = Modifier.height(16.dp))
        PostTaskScoreLine(
            assignedScore = post.assignedScore,
            maxScore = post.taskDetails.maxScore,
            compact = false,
            showMaxWhenUngraded = false,
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
                PostFileAttachmentCard(
                    attachment = attachment,
                    onClick =
                        attachment.id?.let { id ->
                            { onAttachmentDownload(id) }
                        },
                )
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
    onAttachmentDownload: (fileId: UUID) -> Unit = {},
) {
    Column(modifier = modifier) {
        PostHeaderUserCourseCard(
            title = post.title,
            typeLabel = post.typeLabelForScreen(),
            icon = Icons.Outlined.Groups,
        )
        Spacer(modifier = Modifier.height(8.dp))
        PostHeaderDetailsCollapsible(
            createdAt = post.createdAt,
            deadline = post.taskDetails.deadline,
            teamCompositionLabel = teamCompositionRulesLabel(post),
            maxScore = post.taskDetails.maxScore,
        )
        Spacer(modifier = Modifier.height(16.dp))
        PostTaskScoreLine(
            assignedScore = post.assignedScore,
            maxScore = post.taskDetails.maxScore,
            compact = false,
            showMaxWhenUngraded = false,
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
                PostFileAttachmentCard(
                    attachment = attachment,
                    onClick =
                        attachment.id?.let { id ->
                            { onAttachmentDownload(id) }
                        },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private fun teamCompositionRulesLabel(post: TeamTaskPost): String? =
    when {
        post.minTeamSize != null && post.maxTeamSize != null ->
            "Состав команды: от ${post.minTeamSize} до ${post.maxTeamSize} человек"
        post.minTeamSize != null -> "Минимум участников в команде: ${post.minTeamSize}"
        post.maxTeamSize != null -> "Максимум участников в команде: ${post.maxTeamSize}"
        else -> null
    }

/** Как [com.stuf.classroom.course.components.feed.CourseFeedPostCardShell]: Card + отступы 16.dp. */
private val PostDetailsCardElevation = 2.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostHeaderDetailsCollapsible(
    createdAt: OffsetDateTime,
    deadline: OffsetDateTime?,
    teamCompositionLabel: String?,
    maxScore: Int?,
    modifier: Modifier = Modifier,
) {
    var expanded: Boolean by rememberSaveable { mutableStateOf(false) }
    val formatter =
        remember {
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withLocale(Locale.getDefault())
        }
    val detailTextStyle = MaterialTheme.typography.bodyMedium
    val detailColor = MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        onClick = { expanded = !expanded },
        modifier =
            modifier
                .fillMaxWidth()
                .testTag("post_header_details_toggle"),
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = PostDetailsCardElevation),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Детали",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Свернуть" else "Развернуть",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Создано: ${createdAt.format(formatter)}",
                        style = detailTextStyle,
                        color = detailColor,
                        modifier = Modifier.testTag("post_header_created_at"),
                    )
                    if (deadline != null) {
                        Text(
                            text = "Срок сдачи: ${deadline.format(formatter)}",
                            style = detailTextStyle,
                            color = detailColor,
                            modifier = Modifier.testTag("post_header_deadline"),
                        )
                    }
                    maxScore?.let { max ->
                        Text(
                            text = "Максимум баллов: $max",
                            style = detailTextStyle,
                            color = detailColor,
                            modifier = Modifier.testTag("post_header_max_score"),
                        )
                    }
                    teamCompositionLabel?.let { label ->
                        Text(
                            text = label,
                            modifier = Modifier.testTag("team_task_rules"),
                            style = detailTextStyle,
                            color = detailColor,
                        )
                    }
                }
            }
        }
    }
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
    onClick: (() -> Unit)? = null,
) {
    val subtitle =
        if (onClick != null) {
            "Нажмите, чтобы скачать"
        } else {
            "Вложение"
        }
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("post_file_card")
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                ),
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
                text = subtitle,
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
