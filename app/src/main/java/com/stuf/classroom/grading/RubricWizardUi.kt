package com.stuf.classroom.grading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stuf.classroom.selfassessment.SelfAssessmentFormStep
import com.stuf.grading.domain.model.CriterionDefinition
import com.stuf.grading.domain.model.CriterionDirection
import com.stuf.grading.domain.model.CriterionId
import com.stuf.grading.domain.model.SelfAssessmentDraft
import com.stuf.grading.domain.model.TaskGradingRubric
import com.stuf.grading.domain.preview.RubricPreviewResult
import java.util.Locale
import kotlin.math.abs

data class RubricWizardUiState(
    val rubric: TaskGradingRubric,
    val draft: SelfAssessmentDraft,
    val localPreview: RubricPreviewResult,
    val formSteps: List<SelfAssessmentFormStep>,
    val wizardStepIndex: Int,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val showStudentScoreWeightHint: Boolean = true,
)

@Composable
fun RubricWizardBody(
    state: RubricWizardUiState,
    onWeightedScoreChange: (CriterionId, Double) -> Unit,
    onToggleChange: (CriterionId, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rubric = state.rubric
    val steps = state.formSteps
    val isResult = steps.isEmpty() || state.wizardStepIndex >= steps.size
    val total = if (steps.isEmpty()) 1 else steps.size + 1
    val progress =
        when {
            steps.isEmpty() -> 1f
            isResult -> 1f
            else -> (state.wizardStepIndex + 1f) / total
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 4.dp),
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        state.saveError?.let { err ->
            Text(
                text = err,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        when {
            isResult ->
                RubricWizardResultContent(
                    rubric = rubric,
                    localPreview = state.localPreview,
                    resultHint = "Итог по вашей оценке по рубрике задания.",
                )
            else -> {
                when (val step = steps[state.wizardStepIndex]) {
                    is SelfAssessmentFormStep.Weighted ->
                        RubricWeightedStepContent(
                            def = step.criterion,
                            value = state.draft.weightedScores[step.criterion.id] ?: 0.0,
                            rubric = rubric,
                            showStudentScoreWeightHint = state.showStudentScoreWeightHint,
                            onValueChange = { onWeightedScoreChange(step.criterion.id, it) },
                        )
                    SelfAssessmentFormStep.BonusPenaltySection ->
                        RubricBonusPenaltySectionContent(
                            rubric = rubric,
                            toggles = state.draft.toggledEnabled,
                            onToggleChange = onToggleChange,
                        )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun RubricWizardBottomBar(
    state: RubricWizardUiState,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    onSubmit: () -> Unit,
    submitLabel: String,
    modifier: Modifier = Modifier,
) {
    val steps = state.formSteps
    val isResult = steps.isEmpty() || state.wizardStepIndex >= steps.size
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f),
            ) {
                Text("Назад")
            }
            if (!isResult) {
                val isLastForm = steps.isNotEmpty() && state.wizardStepIndex == steps.size - 1
                Button(
                    onClick = onNavigateNext,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (isLastForm) "К результату" else "Далее")
                }
            } else {
                Button(
                    onClick = onSubmit,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (state.isSaving) "Отправка…" else submitLabel)
                }
            }
        }
    }
}

@Composable
private fun RubricWeightedStepContent(
    def: CriterionDefinition.Weighted,
    value: Double,
    rubric: TaskGradingRubric,
    showStudentScoreWeightHint: Boolean,
    onValueChange: (Double) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = rubric.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = def.title,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text =
                "Оцените выполнение по шкале от 0 до ${formatNum(def.maxScore)}. " +
                    "Вес критерия: ${formatNum(def.weight)}.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (showStudentScoreWeightHint && rubric.studentScoreWeight > 0) {
            Text(
                text = "Вес оценки в итоговой формуле: ${formatPercent(rubric.studentScoreWeight)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Текущее значение", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${formatNum(value)} / ${formatNum(def.maxScore)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = 0f..def.maxScore.toFloat(),
        )
    }
}

@Composable
private fun RubricBonusPenaltySectionContent(
    rubric: TaskGradingRubric,
    toggles: Map<CriterionId, Boolean>,
    onToggleChange: (CriterionId, Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(text = "Бонусы и штрафы", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Включите пункт, если он относится к оцениваемой работе.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        rubric.criteria.filterIsInstance<CriterionDefinition.ManualBonusPenalty>().forEach { c ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = c.title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text =
                            when (c.direction) {
                                CriterionDirection.ADD -> "+${formatNum(c.points)} балл"
                                CriterionDirection.SUBTRACT -> "−${formatNum(c.points)} балл"
                            },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = toggles[c.id] == true,
                    onCheckedChange = { onToggleChange(c.id, it) },
                )
            }
        }
    }
}

@Composable
fun RubricWizardResultContent(
    rubric: TaskGradingRubric,
    localPreview: RubricPreviewResult,
    resultHint: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Расчёт по критериям", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = resultHint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "${formatNum(localPreview.finalScore)} из ${formatNum(rubric.assignmentMaxScore)}",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

private fun formatNum(v: Double): String {
    val rounded = (v * 100).toInt() / 100.0
    return if (abs(rounded - rounded.toLong()) < 0.001) {
        rounded.toLong().toString()
    } else {
        String.format(Locale.getDefault(), "%.2f", rounded)
    }
}

private fun formatPercent(v: Double): String = "${(v * 100).toInt()}%"
