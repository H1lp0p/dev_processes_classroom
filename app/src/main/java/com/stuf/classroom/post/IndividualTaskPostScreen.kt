package com.stuf.classroom.post

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.stuf.classroom.post.components.PostCommentItem
import com.stuf.classroom.post.components.PostFileAttachmentCard
import com.stuf.classroom.post.components.PostScreenCommentsDivider
import com.stuf.classroom.post.components.PostScreenTaskSection
import com.stuf.classroom.post.components.PostScreenTopBar
import com.stuf.domain.model.CommentId
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.FileInfo
import com.stuf.domain.model.PostAttachment
import com.stuf.domain.model.Solution
import com.stuf.domain.model.TaskPost
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun IndividualTaskPostScreen(
    state: PostUiState,
    post: TaskPost,
    onBackClick: () -> Unit,
    onOpenPublicCommentComposer: () -> Unit,
    onEditCommentClick: (CommentId, String, Boolean) -> Unit,
    onDeleteCommentClick: (CommentId, Boolean) -> Unit,
    onReplyClick: (CommentId) -> Unit,
    onLoadRepliesClick: (CommentId) -> Unit,
    onPickSolutionFile: () -> Unit,
    onSubmitIndividualSolution: (String) -> Unit,
    onDeleteIndividualSolution: () -> Unit,
    onRemovePendingIndividualSolutionFile: (String) -> Unit,
    onRemoveSavedIndividualSolutionFile: (String) -> Unit,
    onDownloadAttachment: (UUID) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val ind: IndividualTaskPostState = state.individualTask ?: IndividualTaskPostState()
    val publicComments: List<CommentUi> = state.comments.filter { !it.isPrivate }
    val showStudentActions: Boolean = state.currentUserRole == CourseRole.STUDENT

    if (!showStudentActions) {
        TeacherIndividualTaskPostScreen(
            state = state,
            post = post,
            onBackClick = onBackClick,
            onOpenPublicCommentComposer = onOpenPublicCommentComposer,
            onEditCommentClick = onEditCommentClick,
            onDeleteCommentClick = onDeleteCommentClick,
            onReplyClick = onReplyClick,
            onLoadRepliesClick = onLoadRepliesClick,
            modifier = modifier,
        )
        return
    }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val sheetPeekHeight = 132.dp

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = sheetPeekHeight,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        sheetShadowElevation = 18.dp,
        sheetTonalElevation = 6.dp,
        containerColor = MaterialTheme.colorScheme.background,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetContent = {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                ) {
                    IndividualSheetPeekSummary(
                        isLoading = ind.isLoadingSolutionSection,
                        solution = ind.solution,
                    )
                    ind.sectionError?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    val deadlineFormatter =
                        remember {
                            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withLocale(Locale.getDefault())
                        }
                    val assignmentDeadline = post.taskDetails.deadline
                    val isDeadlinePassed =
                        assignmentDeadline?.isBefore(OffsetDateTime.now()) == true
                    val deadlineLabel = assignmentDeadline?.format(deadlineFormatter)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = "Решение",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        deadlineLabel?.let { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Normal,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    IndividualSolutionBlock(
                        solution = ind.solution,
                        pendingFiles = ind.pendingSolutionFiles,
                        removedSavedFileIds = ind.removedSavedSolutionFileIds,
                        showStudentActions = showStudentActions,
                        isDeadlinePassed = isDeadlinePassed,
                        onPickFile = onPickSolutionFile,
                        onSubmit = onSubmitIndividualSolution,
                        onDelete = onDeleteIndividualSolution,
                        onRemovePendingFile = onRemovePendingIndividualSolutionFile,
                        onRemoveSavedFile = onRemoveSavedIndividualSolutionFile,
                        onDownloadAttachment = onDownloadAttachment,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
            ) {
                PostScreenTopBar(onBackClick = onBackClick)
            }
            LazyColumn(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("individual_task_post_main"),
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item {
                    PostScreenTaskSection(
                        post = post,
                        onAttachmentDownload = onDownloadAttachment,
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    PostScreenCommentsDivider()
                }
                item {
                    Text(
                        text = "Публичные комментарии",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (state.isLoadingComments) {
                    item {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                state.commentsLoadError?.let { msg ->
                    item {
                        Text(text = msg, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                item {
                    TextButton(
                        onClick = onOpenPublicCommentComposer,
                        modifier = Modifier.testTag("post_new_comment_button"),
                    ) {
                        Text("Написать комментарий")
                    }
                }
                items(
                    items = publicComments,
                    key = { it.id },
                ) { comment ->
                    PostCommentItem(
                        comment = comment,
                        onLoadRepliesClick = onLoadRepliesClick,
                        onReplyClick = onReplyClick,
                        onEditCommentClick = onEditCommentClick,
                        onDeleteCommentClick = onDeleteCommentClick,
                        currentUserId = state.currentUserId?.value?.toString(),
                        loadingRepliesForCommentId = state.loadingRepliesForCommentId,
                    )
                }
            }
        }
    }
}

@Composable
private fun TeacherIndividualTaskPostScreen(
    state: PostUiState,
    post: TaskPost,
    onBackClick: () -> Unit,
    onOpenPublicCommentComposer: () -> Unit,
    onEditCommentClick: (CommentId, String, Boolean) -> Unit,
    onDeleteCommentClick: (CommentId, Boolean) -> Unit,
    onReplyClick: (CommentId) -> Unit,
    onLoadRepliesClick: (CommentId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val publicComments: List<CommentUi> = state.comments.filter { !it.isPrivate }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
        ) {
            PostScreenTopBar(onBackClick = onBackClick)
        }
        LazyColumn(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("individual_task_post_main"),
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                PostScreenTaskSection(post = post)
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Подробные данные по заданию и решения студентов доступны на веб-сайте.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                PostScreenCommentsDivider()
            }
            item {
                Text(
                    text = "Публичные комментарии",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (state.isLoadingComments) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            state.commentsLoadError?.let { msg ->
                item {
                    Text(text = msg, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            item {
                TextButton(
                    onClick = onOpenPublicCommentComposer,
                    modifier = Modifier.testTag("post_new_comment_button"),
                ) {
                    Text("Написать комментарий")
                }
            }
            items(
                items = publicComments,
                key = { it.id },
            ) { comment ->
                PostCommentItem(
                    comment = comment,
                    onLoadRepliesClick = onLoadRepliesClick,
                    onReplyClick = onReplyClick,
                    onEditCommentClick = onEditCommentClick,
                    onDeleteCommentClick = onDeleteCommentClick,
                    currentUserId = state.currentUserId?.value?.toString(),
                    loadingRepliesForCommentId = state.loadingRepliesForCommentId,
                )
            }
        }
    }
}

@Composable
private fun IndividualSheetPeekSummary(
    isLoading: Boolean,
    solution: Solution?,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        when {
            isLoading ->
                Text(
                    text = "Загрузка…",
                    style = MaterialTheme.typography.bodyLarge,
                )
            solution == null ->
                Text(
                    text = "Решение ещё не отправлено",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            else -> {
                val formatter =
                    remember {
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withLocale(Locale.getDefault())
                    }
                val submittedAt = solution.updatedAt.format(formatter)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Отправлено",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = submittedAt,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun IndividualSolutionBlock(
    solution: Solution?,
    pendingFiles: List<FileInfo>,
    removedSavedFileIds: Set<String>,
    showStudentActions: Boolean,
    isDeadlinePassed: Boolean,
    onPickFile: () -> Unit,
    onSubmit: (String) -> Unit,
    onDelete: () -> Unit,
    onRemovePendingFile: (String) -> Unit,
    onRemoveSavedFile: (String) -> Unit,
    onDownloadAttachment: (UUID) -> Unit,
) {
    val canEdit: Boolean = showStudentActions && !isDeadlinePassed
    val visibleSavedFiles: List<FileInfo> =
        solution?.files?.filter { it.id !in removedSavedFileIds }.orEmpty()

    var draftText: String by remember(solution?.updatedAt, solution?.id) {
        mutableStateOf(solution?.text.orEmpty())
    }

    if (isDeadlinePassed) {
        Text(
            text = "Срок сдачи прошёл",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    solution?.score?.let { sc ->
        Text(
            text = "Оценка: ${sc.value}",
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    when {
        solution == null && showStudentActions -> {
            OutlinedTextField(
                value = draftText,
                onValueChange = { if (canEdit) draftText = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = canEdit,
                minLines = 3,
                maxLines = 10,
                label = { Text("Текст решения") },
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onPickFile,
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Добавить файл")
            }
            pendingFiles.forEach { f: FileInfo ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PostFileAttachmentCard(
                            attachment = f.toPostAttachment(),
                            onClick = solutionFileDownloadClick(f, onDownloadAttachment),
                        )
                    }
                    if (canEdit) {
                        IconButton(onClick = { onRemovePendingFile(f.id) }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Убрать файл",
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = { onSubmit(draftText) },
                enabled =
                    canEdit &&
                        (draftText.isNotBlank() || pendingFiles.isNotEmpty()),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("individual_task_submit_solution"),
            ) {
                Text("Отправить решение")
            }
        }
        solution == null && !showStudentActions ->
            Text(
                text = "Студент ещё не отправил решение.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        solution != null -> {
            if (showStudentActions) {
                if (canEdit) {
                    OutlinedTextField(
                        value = draftText,
                        onValueChange = { draftText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 10,
                        label = { Text("Текст решения") },
                    )
                } else {
                    solution.text?.let { t ->
                        Text(text = t, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                solution.text?.let { t ->
                    Text(text = t, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (visibleSavedFiles.isNotEmpty() || pendingFiles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            visibleSavedFiles.forEach { f: FileInfo ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PostFileAttachmentCard(
                            attachment = f.toPostAttachment(),
                            onClick = solutionFileDownloadClick(f, onDownloadAttachment),
                        )
                    }
                    if (canEdit) {
                        IconButton(onClick = { onRemoveSavedFile(f.id) }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Удалить файл из решения",
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            pendingFiles.forEach { f: FileInfo ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PostFileAttachmentCard(
                            attachment = f.toPostAttachment(),
                            onClick = solutionFileDownloadClick(f, onDownloadAttachment),
                        )
                    }
                    if (canEdit) {
                        IconButton(onClick = { onRemovePendingFile(f.id) }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Убрать файл",
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (showStudentActions) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onPickFile,
                    enabled = canEdit,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Добавить файл")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onSubmit(draftText) },
                    enabled = canEdit,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag("individual_task_save_solution"),
                ) {
                    Text("Сохранить решение")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onDelete,
                    enabled = canEdit,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag("individual_task_delete_solution"),
                ) {
                    Text("Удалить решение")
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

private fun solutionFileDownloadClick(
    f: FileInfo,
    onDownloadAttachment: (UUID) -> Unit,
): (() -> Unit)? {
    val id = runCatching { UUID.fromString(f.id) }.getOrNull() ?: return null
    return { onDownloadAttachment(id) }
}
