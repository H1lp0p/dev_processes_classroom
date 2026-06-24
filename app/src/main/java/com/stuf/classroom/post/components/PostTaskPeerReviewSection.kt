package com.stuf.classroom.post.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stuf.domain.model.PeerReviewPostInfo
import com.stuf.domain.model.PeerReviewProgress
import com.stuf.domain.model.navigationBlockMessage
import com.stuf.domain.model.peerReviewSectionHint

@Composable
internal fun PostTaskPeerReviewSection(
    peerReviewInfo: PeerReviewPostInfo,
    progress: PeerReviewProgress?,
    hasSolution: Boolean,
    isTeamTask: Boolean,
    onOpenPeerReview: () -> Unit,
    onFinishPeerReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!peerReviewInfo.isPeerToPeer) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "P2P-оценивание",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        val sectionHint = peerReviewInfo.peerReviewSectionHint()
        if (sectionHint.isNotBlank()) {
            Text(
                text = sectionHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        progress?.let { p ->
            PeerReviewProgressCard(progress = p, isTeamTask = isTeamTask)
        }

        val blockMessage = peerReviewInfo.navigationBlockMessage(hasSolution)
        if (blockMessage != null) {
            Text(
                text = blockMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Button(
                onClick = onOpenPeerReview,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("post_open_peer_review_button"),
            ) {
                Text(if (isTeamTask) "Оценить решение команды" else "Оценить работу")
            }
        }

        if (!isTeamTask && progress?.canFinish == true && progress.isCounted.not()) {
            OutlinedButton(
                onClick = onFinishPeerReview,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("post_finish_peer_review_button"),
            ) {
                Text("Завершить оценивание")
            }
        }
    }
}

@Composable
private fun PeerReviewProgressCard(
    progress: PeerReviewProgress,
    isTeamTask: Boolean,
) {
    val fraction =
        if (progress.required > 0) {
            (progress.completed.toFloat() / progress.required).coerceIn(0f, 1f)
        } else {
            0f
        }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "${progress.completed} из ${progress.required} оценок",
                style = MaterialTheme.typography.titleSmall,
            )
            LinearProgressIndicator(
                progress = { fraction },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp),
            )
            Text(
                text =
                    when {
                        progress.isCounted -> "Решение засчитано"
                        progress.canFinish && !isTeamTask -> "Можно завершить оценивание"
                        else -> "Оценивание не завершено"
                    },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
