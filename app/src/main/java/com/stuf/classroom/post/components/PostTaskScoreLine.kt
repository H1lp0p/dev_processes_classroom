package com.stuf.classroom.post.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.stuf.domain.model.Score

/**
 * Оценка по заданию и максимум баллов ([TaskDetails.maxScore]).
 *
 * @param compact краткий вид для карточки ленты курса.
 */
@Composable
internal fun PostTaskScoreLine(
    assignedScore: Score?,
    maxScore: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    /**
     * Если false и выставленной оценки нет, строка скрыта (максимум показывается в блоке «Детали» на экране поста).
     */
    showMaxWhenUngraded: Boolean = true,
) {
    if (!showMaxWhenUngraded && assignedScore == null) {
        return
    }
    val text: String =
        when {
            assignedScore != null && compact -> "${assignedScore.value} / $maxScore"
            assignedScore != null && !compact -> "Оценка: ${assignedScore.value} из $maxScore"
            compact -> "макс. $maxScore"
            else -> "Максимум баллов: $maxScore"
        }
    Text(
        text = text,
        modifier = modifier.testTag("post_task_score"),
        style =
            if (compact) {
                MaterialTheme.typography.labelSmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
