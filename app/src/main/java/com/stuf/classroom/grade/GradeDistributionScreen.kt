package com.stuf.classroom.grade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
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
    val showBottomBar: Boolean = !state.isLoading && state.loadError == null
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
                Text(
                    text = "Распределение оценок",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                GradeDistributionBottomBar(
                    state = state,
                    onSave = onSave,
                    onVote = onVote,
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
            else -> {
                val memberRowSpacing =
                    remember(state.isCaptain) {
                        if (state.isCaptain) 12.dp else 20.dp
                    }
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = "Баллов у команды: ${formatDouble(state.teamRawScore)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(memberRowSpacing),
                    ) {
                        state.members.forEach { row ->
                            GradeDistributionMemberRowContent(
                                row = row,
                                state = state,
                                onDraftChange = onDraftChange,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    GradeDistributionRemainderRow(
                        remainder = state.remainder,
                        remainderNegative = state.remainderNegative,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun GradeDistributionBottomBar(
    state: GradeDistributionUiState,
    onSave: () -> Unit,
    onVote: (GradeVote) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 8.dp),
    ) {
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
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val gap = 12.dp
                        val half = (maxWidth - gap) / 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(gap),
                        ) {
                            OutlinedButton(
                                onClick = { onVote(GradeVote.FOR) },
                                enabled = !state.isVoting,
                                modifier = Modifier.width(half),
                            ) {
                                Text("За")
                            }
                            OutlinedButton(
                                onClick = { onVote(GradeVote.AGAINST) },
                                enabled = !state.isVoting,
                                modifier = Modifier.width(half),
                            ) {
                                Text("Против")
                            }
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
    }
}

@Composable
private fun GradeDistributionMemberRowContent(
    row: GradeDistributionMemberRow,
    state: GradeDistributionUiState,
    onDraftChange: (UserId, String) -> Unit,
) {
    val key = row.userId.value.toString()
    if (state.isCaptain) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val gap = 8.dp
            val fieldMax = 140.dp
            val fieldW = minOf(fieldMax, maxWidth * 0.42f)
            val nameW = (maxWidth - fieldW - gap).coerceAtLeast(0.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = row.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(nameW),
                )
                OutlinedTextField(
                    value = state.draftPoints[key].orEmpty(),
                    onValueChange = { onDraftChange(row.userId, it) },
                    modifier = Modifier.width(fieldW),
                    singleLine = true,
                    label = { Text("Баллы") },
                )
            }
        }
    } else {
        val pts = state.draftPoints[key].orEmpty()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = row.displayName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .fillMaxWidth(0.65f)
                        .padding(end = 8.dp),
            )
            Text(
                text = if (pts.isNotEmpty()) pts else "—",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun GradeDistributionRemainderRow(
    remainder: Double,
    remainderNegative: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Остаток",
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = formatDouble(remainder),
            style = MaterialTheme.typography.bodyLarge,
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
