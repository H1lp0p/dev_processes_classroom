package com.stuf.classroom.peerreview

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stuf.classroom.grading.RubricWizardBody
import com.stuf.classroom.grading.RubricWizardBottomBar
import com.stuf.domain.model.PeerReviewTeamTarget
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PeerReviewScreen(
    state: PeerReviewUiState,
    wizardState: com.stuf.classroom.grading.RubricWizardUiState?,
    onNavigateUp: () -> Unit,
    onWizardNavigateBack: () -> Boolean,
    onWizardNavigateNext: () -> Unit,
    onWeightedScoreChange: (com.stuf.grading.domain.model.CriterionId, Double) -> Unit,
    onToggleChange: (com.stuf.grading.domain.model.CriterionId, Boolean) -> Unit,
    onSubmitReview: () -> Unit,
    onTeamTargetSelected: (PeerReviewTeamTarget) -> Unit,
    onDownloadFile: (java.util.UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showWizard = wizardState != null && state.loadError == null && !state.isLoading
    val showTeamPicker =
        state.mode == PeerReviewMode.TEAM &&
            state.selectedTeamTarget == null &&
            state.availableTeamTargets.isNotEmpty()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
            ) {
                RowWithBack(title = "P2P-оценка", subtitle = state.taskTitle, onNavigateUp = onNavigateUp)
            }
        },
        bottomBar = {
            if (showWizard && !showTeamPicker) {
                wizardState?.let { ws ->
                    RubricWizardBottomBar(
                        state = ws,
                        onNavigateBack = {
                            if (onWizardNavigateBack()) onNavigateUp()
                        },
                        onNavigateNext = onWizardNavigateNext,
                        onSubmit = onSubmitReview,
                        submitLabel = "Отправить оценку",
                    )
                }
            }
        },
    ) { padding ->
        when {
            state.isLoading ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            state.loadError != null ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                ) {
                    Text(text = state.loadError, color = MaterialTheme.colorScheme.error)
                }
            showTeamPicker ->
                TeamTargetPicker(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    targets = state.availableTeamTargets,
                    onSelect = onTeamTargetSelected,
                )
            showWizard ->
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp),
                ) {
                    AnonymizedSolutionSection(
                        title = "Решение для оценки",
                        solution = state.anonymizedSolution,
                        teamName = state.selectedTeamTarget?.teamName,
                        onDownloadFile = onDownloadFile,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Оценка по критериям",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    wizardState?.let { ws ->
                        RubricWizardBody(
                            state = ws,
                            onWeightedScoreChange = onWeightedScoreChange,
                            onToggleChange = onToggleChange,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
        }
    }
}

@Composable
private fun RowWithBack(
    title: String,
    subtitle: String,
    onNavigateUp: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onNavigateUp) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TeamTargetPicker(
    targets: List<PeerReviewTeamTarget>,
    onSelect: (PeerReviewTeamTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withLocale(Locale.getDefault())
    Column(modifier = modifier) {
        Text(
            text = "Выберите команду для оценки",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (targets.isEmpty()) {
            Text(
                text = "Нет доступных команд для оценки",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(targets, key = { it.teamSolutionId.value }) { target ->
                    OutlinedButton(
                        onClick = { onSelect(target) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(text = target.teamName, fontWeight = FontWeight.Medium)
                            Text(
                                text = "Отправлено: ${target.submittedAt.format(formatter)}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}
