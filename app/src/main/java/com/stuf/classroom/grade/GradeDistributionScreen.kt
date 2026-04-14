package com.stuf.classroom.grade

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.stuf.domain.model.GradeVote
import com.stuf.domain.model.UserId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeDistributionScreen(
    state: GradeDistributionUiState,
    onBack: () -> Unit,
    onDraftChange: (UserId, String) -> Unit,
    onSave: () -> Unit,
    onVote: (GradeVote) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Распределение оценок") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
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
                    CircularProgressIndicator()
                }
            state.loadError != null ->
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                ) {
                    Text(text = state.loadError, color = MaterialTheme.colorScheme.error)
                }
            else ->
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp),
                ) {
                    RowRemainder(
                        remainder = state.remainder,
                        remainderNegative = state.remainderNegative,
                        teamRawScore = state.teamRawScore,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.members, key = { it.userId.value }) { row ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = row.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (state.isCaptain) {
                                    val key = row.userId.value.toString()
                                    OutlinedTextField(
                                        value = state.draftPoints[key].orEmpty(),
                                        onValueChange = { onDraftChange(row.userId, it) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        label = { Text("Баллы") },
                                    )
                                } else {
                                    val key = row.userId.value.toString()
                                    val pts = state.draftPoints[key].orEmpty()
                                    Text(
                                        text = if (pts.isNotEmpty()) pts else "—",
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            }
                        }
                    }
                    state.saveError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (state.isCaptain) {
                        Button(
                            onClick = onSave,
                            enabled = !state.remainderNegative && !state.isSaving,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .testTag("grade_distribution_save"),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (state.isSaving) {
                                    CircularProgressIndicator(
                                        modifier =
                                            Modifier
                                                .size(20.dp)
                                                .padding(end = 8.dp),
                                        strokeWidth = 2.dp,
                                    )
                                }
                                Text(if (state.isSaving) "Сохранение…" else "Сохранить")
                            }
                        }
                    } else {
                        when (state.currentUserVote) {
                            null -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    OutlinedButton(
                                        onClick = { onVote(GradeVote.FOR) },
                                        enabled = !state.isVoting,
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Text("За")
                                    }
                                    OutlinedButton(
                                        onClick = { onVote(GradeVote.AGAINST) },
                                        enabled = !state.isVoting,
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Text("Против")
                                    }
                                }
                            }
                            GradeVote.FOR ->
                                Text(
                                    text = "Вы проголосовали за это распределение",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            GradeVote.AGAINST ->
                                Text(
                                    text = "Вы проголосовали против этого распределения",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
        }
    }
}

@Composable
private fun RowRemainder(
    remainder: Double,
    remainderNegative: Boolean,
    teamRawScore: Double,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Баллов у команды (Rraw): $teamRawScore",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Остаток: ${formatDouble(remainder)}",
            style = MaterialTheme.typography.titleMedium,
            color =
                if (remainderNegative) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
        )
    }
}

private fun formatDouble(v: Double): String {
    if (kotlin.math.abs(v - v.toLong()) < 1e-9) return v.toLong().toString()
    return String.format(java.util.Locale.US, "%.2f", v).trimEnd('0').trimEnd('.')
}
