package com.stuf.classroom.peerreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stuf.classroom.post.components.PostFileAttachmentCard
import com.stuf.domain.model.AnonymizedSolution
import com.stuf.domain.model.FileInfo
import com.stuf.domain.model.PostAttachment
import java.util.UUID

@Composable
fun AnonymizedSolutionSection(
    title: String,
    solution: AnonymizedSolution?,
    teamName: String? = null,
    onDownloadFile: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            teamName?.let {
                Text(
                    text = "Команда: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "Автор скрыт — оценивание анонимное",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (solution == null) {
                Text(
                    text = "Текст решения недоступен в приложении. Оцените работу по рубрике ниже.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                solution.text?.takeIf { it.isNotBlank() }?.let { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                } ?: Text(
                    text = "Текст решения не указан",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (solution.files.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    solution.files.forEach { file ->
                        PostFileAttachmentCard(
                            attachment = file.toPostAttachment(),
                            onClick = file.downloadClick(onDownloadFile),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

private fun FileInfo.toPostAttachment(): PostAttachment =
    PostAttachment(
        id = runCatching { UUID.fromString(id) }.getOrNull(),
        name = name,
    )

private fun FileInfo.downloadClick(onDownload: (UUID) -> Unit): (() -> Unit)? {
    val uuid = runCatching { UUID.fromString(id) }.getOrNull() ?: return null
    return { onDownload(uuid) }
}
