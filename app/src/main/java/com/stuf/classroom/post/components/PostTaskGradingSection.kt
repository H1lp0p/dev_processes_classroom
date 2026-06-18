package com.stuf.classroom.post.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stuf.domain.model.SelfAssessmentPostInfo
import com.stuf.domain.model.TeamTaskSolution
import com.stuf.domain.model.UserId
import com.stuf.domain.model.Solution
import com.stuf.grading.domain.model.SelfAssessmentDraft
import com.stuf.grading.domain.model.TaskGradingRubric
import com.stuf.grading.domain.preview.RubricPreviewEngine
import com.stuf.grading.domain.preview.RubricPreviewResult
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Блок оценивания в нижней панели задания: превью сохранённой самооценки,
 * переход к редактированию, для командных заданий — прогресс команды и распределение оценок.
 */
@Composable
internal fun PostTaskGradingSection(
    rubric: TaskGradingRubric?,
    selfAssessmentInfo: SelfAssessmentPostInfo,
    savedSelfAssessmentDraft: SelfAssessmentDraft?,
    hasSolution: Boolean,
    isTeamTask: Boolean,
    isSolutionChecked: Boolean,
    showGradeDistribution: Boolean,
    teamSelfAssessmentSubmittedCount: Int,
    teamSelfAssessmentTotalCount: Int,
    onOpenSelfAssessment: () -> Unit,
    onOpenGradeDistribution: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showSelfAssessment = selfAssessmentInfo.isConfigured
    if (!showSelfAssessment && !showGradeDistribution) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Оценка",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        if (isTeamTask && teamSelfAssessmentTotalCount > 0) {
            PostTeamSelfAssessmentProgressCard(
                submittedCount = teamSelfAssessmentSubmittedCount,
                totalCount = teamSelfAssessmentTotalCount,
            )
        }

        if (showSelfAssessment && savedSelfAssessmentDraft != null && rubric != null) {
            val preview =
                remember(rubric, savedSelfAssessmentDraft) {
                    RubricPreviewEngine.preview(rubric, savedSelfAssessmentDraft)
                }
            PostSavedSelfAssessmentPreview(
                rubric = rubric,
                preview = preview,
            )
        }

        if (showSelfAssessment) {
            PostSelfAssessmentEntry(
                info = selfAssessmentInfo,
                hasSolution = hasSolution,
                isTeamTask = isTeamTask,
                isSolutionChecked = isSolutionChecked,
                onOpenSelfAssessment = onOpenSelfAssessment,
                buttonLabel =
                    if (savedSelfAssessmentDraft != null) {
                        "Изменить самооценку"
                    } else {
                        "Самооценка по критериям"
                    },
            )
        }

        if (showGradeDistribution) {
            OutlinedButton(
                onClick = onOpenGradeDistribution,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("team_task_grade_distribution_button"),
            ) {
                Text("Распределение оценок")
            }
        }
    }
}

@Composable
private fun PostSavedSelfAssessmentPreview(
    rubric: TaskGradingRubric,
    preview: RubricPreviewResult,
) {
    val max = rubric.assignmentMaxScore
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("post_saved_self_assessment_preview"),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Ваша самооценка",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${formatGradingScore(preview.finalScore)} из ${formatGradingScore(max)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Ориентировочный итог по рубрике (до проверки учителем).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (preview.zeroedByFailThreshold) {
                Text(
                    text = "Сработал порог незачёта: итог обнулён.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (preview.boostedToMaxBySuccessThreshold) {
                Text(
                    text = "Сработал порог успеха: итог поднят до максимума.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun PostTeamSelfAssessmentProgressCard(
    submittedCount: Int,
    totalCount: Int,
) {
    val progress =
        if (totalCount > 0) {
            submittedCount.toFloat() / totalCount
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
                text = "Самооценки команды",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "$submittedCount из $totalCount участников сдали самооценку",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp),
            )
        }
    }
}

internal fun resolveSavedSelfAssessmentDraft(
    isTeamTask: Boolean,
    currentUserId: UserId?,
    individualSolution: Solution?,
    teamSolution: TeamTaskSolution?,
): SelfAssessmentDraft? =
    if (isTeamTask) {
        teamSolution
            ?.memberSelfAssessments
            ?.firstOrNull { currentUserId != null && it.userId == currentUserId }
            ?.evaluation
    } else {
        individualSolution?.selfAssessment
    }

internal fun teamSelfAssessmentProgress(
    teamSolution: TeamTaskSolution?,
): Pair<Int, Int> {
    val total = teamSolution?.team?.members?.size ?: 0
    val submitted = teamSolution?.memberSelfAssessments?.count { it.evaluation != null } ?: 0
    return submitted to total
}

private fun formatGradingScore(v: Double): String {
    val rounded = (v * 100).roundToInt() / 100.0
    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        String.format(Locale.getDefault(), "%.2f", rounded)
    }
}
