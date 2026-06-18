package com.stuf.classroom.selfassessment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stuf.grading.domain.model.CriterionDefinition
import com.stuf.grading.domain.model.CriterionDirection
import com.stuf.grading.domain.model.CriterionId
import com.stuf.grading.domain.model.TaskGradingRubric
import com.stuf.grading.domain.preview.RubricPreviewResult
import java.util.Locale
import kotlin.math.abs

@Composable
fun SelfAssessmentScreen(
    state: SelfAssessmentUiState,
    onPop: () -> Unit,
    onWizardNavigateBack: () -> Boolean,
    onWizardNavigateNext: () -> Unit,
    onWeightedScoreChange: (CriterionId, Double) -> Unit,
    onToggleChange: (CriterionId, Boolean) -> Unit,
    onSubmitSelfAssessment: () -> Unit,
    onDeleteSelfAssessment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showWizardBottomBar: Boolean =
        !state.isLoading &&
            state.loadError == null &&
            state.rubric != null &&
            state.localPreview != null &&
            !state.isReadOnly

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SelfAssessmentTopBar(
                state = state,
                onNavigateUp = {
                    if (onWizardNavigateBack()) {
                        onPop()
                    }
                },
            )
        },
        bottomBar = {
            if (showWizardBottomBar) {
                SelfAssessmentWizardBottomBar(
                    state = state,
                    onNavigateBack = {
                        if (onWizardNavigateBack()) {
                            onPop()
                        }
                    },
                    onNavigateNext = onWizardNavigateNext,
                    onSubmitSelfAssessment = onSubmitSelfAssessment,
                )
            }
        },
    ) { padding ->
        when {
            state.isLoading ->
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

            state.loadError != null ->
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(24.dp),
                ) {
                    Text(
                        text = state.loadError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

            state.rubric != null && state.localPreview != null ->
                SelfAssessmentWizardBody(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 20.dp),
                    state = state,
                    onWeightedScoreChange = onWeightedScoreChange,
                    onToggleChange = onToggleChange,
                )
        }
    }
}

@Composable
private fun SelfAssessmentTopBar(
    state: SelfAssessmentUiState,
    onNavigateUp: () -> Unit,
) {
    val rubric = state.rubric
    val title: String
    val subtitle: String?
    if (rubric != null && state.localPreview != null && !state.isLoading) {
        val steps = state.formSteps
        val isResult = steps.isEmpty() || state.wizardStepIndex >= steps.size
        val total = if (steps.isEmpty()) 1 else steps.size + 1
        val current = (state.wizardStepIndex + 1).coerceAtMost(total)
        title =
            if (isResult) {
                "Результат самооценки"
            } else {
                "Самооценка"
            }
        subtitle = "Шаг $current из $total"
    } else {
        title = "Самооценка"
        subtitle = null
    }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                subtitle?.let { s ->
                    Text(
                        text = s,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SelfAssessmentWizardBottomBar(
    state: SelfAssessmentUiState,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    onSubmitSelfAssessment: () -> Unit,
) {
    val rubric = state.rubric ?: return
    val steps = state.formSteps
    val isResult = steps.isEmpty() || state.wizardStepIndex >= steps.size
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
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
                Text("Назад", style = MaterialTheme.typography.titleMedium)
            }
            if (!isResult) {
                val isLastForm = steps.isNotEmpty() && state.wizardStepIndex == steps.size - 1
                Button(
                    onClick = onNavigateNext,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = if (isLastForm) "К результату" else "Далее",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            } else {
                Button(
                    onClick = onSubmitSelfAssessment,
                    enabled = !state.isSaving && !state.isReadOnly,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = if (state.isSaving) "Сохранение…" else "Сохранить самооценку",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun SelfAssessmentWizardBody(
    state: SelfAssessmentUiState,
    onWeightedScoreChange: (CriterionId, Double) -> Unit,
    onToggleChange: (CriterionId, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rubric = state.rubric ?: return
    val localPreview = state.localPreview ?: return
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
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
        if (state.isTeamTask && state.teamSelfAssessmentTotalCount > 0) {
            TeamSelfAssessmentProgressCard(
                submittedCount = state.teamSelfAssessmentSubmittedCount,
                totalCount = state.teamSelfAssessmentTotalCount,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        state.saveError?.let { err ->
            Text(
                text = err,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (state.saveSuccess) {
            Text(
                text = "Самооценка сохранена",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        when {
            isResult ->
                SelfAssessmentResultContent(
                    rubric = rubric,
                    localPreview = localPreview,
                )

            else -> {
                val step = steps[state.wizardStepIndex]
                when (step) {
                    is SelfAssessmentFormStep.Weighted ->
                        WeightedStepContent(
                            def = step.criterion,
                            value = state.draft.weightedScores[step.criterion.id] ?: 0.0,
                            rubric = rubric,
                            onValueChange = { onWeightedScoreChange(step.criterion.id, it) },
                        )

                    SelfAssessmentFormStep.BonusPenaltySection ->
                        BonusPenaltySectionContent(
                            rubric = rubric,
                            toggles = state.draft.toggledEnabled,
                            onToggleChange = onToggleChange,
                        )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun WeightedStepContent(
    def: CriterionDefinition.Weighted,
    value: Double,
    rubric: TaskGradingRubric,
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
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text =
                "Оцените выполнение по шкале от 0 до ${formatNum(def.maxScore)}. " +
                    "Вес критерия в формуле: ${formatNum(def.weight)}.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (rubric.studentScoreWeight > 0) {
            Text(
                text = "Вес самооценки в итоговой оценке: ${formatPercent(rubric.studentScoreWeight)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Текущее значение",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
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
private fun BonusPenaltySectionContent(
    rubric: TaskGradingRubric,
    toggles: Map<CriterionId, Boolean>,
    onToggleChange: (CriterionId, Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Бонусы и штрафы",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Включите пункт, если он относится к вашей работе.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        rubric.criteria.filterIsInstance<CriterionDefinition.ManualBonusPenalty>().forEach { c ->
            ToggleCriterionRowLarge(
                title = c.title,
                subtitle =
                    when (c.direction) {
                        CriterionDirection.ADD -> "+${formatNum(c.points)} балл"
                        CriterionDirection.SUBTRACT -> "−${formatNum(c.points)} балл"
                    },
                checked = toggles[c.id] == true,
                onCheckedChange = { onToggleChange(c.id, it) },
            )
        }
    }
}

@Composable
private fun TeamSelfAssessmentProgressCard(
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
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "$submittedCount из $totalCount сдано",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
private fun SelfAssessmentResultContent(
    rubric: TaskGradingRubric,
    localPreview: RubricPreviewResult,
) {
    val max = rubric.assignmentMaxScore
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Расчёт по критериям",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Ориентировочный итог по рубрике задания (до проверки учителем).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "${formatNum(localPreview.finalScore)} из ${formatNum(max)}",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        if (localPreview.zeroedByFailThreshold) {
            Text(
                text = "Сработал порог незачёта: итог обнулён.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (localPreview.boostedToMaxBySuccessThreshold) {
            Text(
                text = "Сработал порог успеха: итог поднят до максимума за задание.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text = "Разбивка по шагам формулы",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        HorizontalScoreBarsSectionLarge(rubric = rubric, preview = localPreview)
    }
}

@Composable
private fun ToggleCriterionRowLarge(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SelfAssessmentPlaceholder(
    rubricTitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = rubricTitle,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Форма самооценки временно отключена",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text =
                        "Когда будет включён флаг enableTeamReflexyScore, здесь появится " +
                            "многоступенчатая форма и отдельный экран с результатом.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HorizontalScoreBarsSectionLarge(
    rubric: TaskGradingRubric,
    preview: RubricPreviewResult,
) {
    val max = rubric.assignmentMaxScore.takeIf { it > 0 } ?: 1.0
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        preview.weightedContributions.forEach { line ->
            val weightedDef =
                rubric.criteria.filterIsInstance<CriterionDefinition.Weighted>().find { it.id == line.criterionId }
            val defMax = weightedDef?.maxScore ?: max
            ScoreBarRowLarge(
                label = line.title,
                progress = (line.rawScore / defMax).toFloat().coerceIn(0f, 1f),
                valueText = "${formatNum(line.rawScore)} × ${formatNum(line.weight)} = ${formatNum(line.contribution)}",
            )
        }
        val weightedTotal = preview.weightedContributions.sumOf { it.contribution }
        ScoreBarRowLarge(
            label = "Сумма весовых вкладов",
            progress = (abs(weightedTotal) / max).toFloat().coerceIn(0f, 1f),
            valueText = formatNum(weightedTotal),
        )
        ScoreBarRowLarge(
            label = "Ручные бонусы и штрафы",
            progress = (abs(preview.manualToggleDelta) / max).toFloat().coerceIn(0f, 1f),
            valueText = formatSigned(preview.manualToggleDelta),
        )
        if (abs(preview.qualityDelta) > 1e-9) {
            ScoreBarRowLarge(
                label = "Качество (авто)",
                progress = (abs(preview.qualityDelta) / max).toFloat().coerceIn(0f, 1f),
                valueText = formatSigned(preview.qualityDelta),
            )
        }
        ScoreBarRowLarge(
            label = "После модификаторов",
            progress = (preview.scoreAfterModifiers / max).toFloat().coerceIn(0f, 1f),
            valueText = formatNum(preview.scoreAfterModifiers),
        )
        ScoreBarRowLarge(
            label = "После блокирующих",
            progress = (preview.scoreAfterBlocking / max).toFloat().coerceIn(0f, 1f),
            valueText = formatNum(preview.scoreAfterBlocking),
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        ScoreBarRowLarge(
            label = "Итог",
            progress = (preview.finalScore / max).toFloat().coerceIn(0f, 1f),
            valueText = "${formatNum(preview.finalScore)} / ${formatNum(max)}",
            emphasize = true,
        )
    }
}

@Composable
private fun ScoreBarRowLarge(
    label: String,
    progress: Float,
    valueText: String,
    emphasize: Boolean = false,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style =
                    if (emphasize) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(if (emphasize) 12.dp else 8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

private fun formatNum(v: Double): String =
    if (kotlin.math.abs(v - v.toLong()) < 1e-9) {
        v.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", v).trimEnd('0').trimEnd('.')
    }

private fun formatPercent(v: Double): String =
    String.format(Locale.getDefault(), "%.0f%%", v * 100.0)

private fun formatSigned(v: Double): String =
    if (v >= 0) "+${formatNum(v)}" else formatNum(v)
