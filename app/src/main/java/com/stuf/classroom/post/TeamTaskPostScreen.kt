package com.stuf.classroom.post

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.stuf.classroom.post.components.PostFileAttachmentCard
import com.stuf.classroom.post.components.PostCommentItem
import com.stuf.classroom.post.components.PostScreenCommentsDivider
import com.stuf.classroom.post.components.PostScreenTopBar
import com.stuf.classroom.post.components.PostScreenTeamTaskSection
import com.stuf.domain.model.FileInfo
import com.stuf.domain.model.PostAttachment
import com.stuf.domain.model.TeamCaptainSelectionMode
import com.stuf.domain.model.CommentId
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.Team
import com.stuf.domain.model.TeamId
import com.stuf.domain.model.TeamMemberRole
import com.stuf.domain.model.UserId
import com.stuf.domain.model.TeamTaskPost
import com.stuf.domain.model.TeamTaskSolution
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TeamTaskPostScreen(
    state: PostUiState,
    post: TeamTaskPost,
    onBackClick: () -> Unit,
    onOpenPublicCommentComposer: () -> Unit,
    onOpenPrivateCommentComposer: () -> Unit,
    onReplyClick: (CommentId) -> Unit,
    onLoadRepliesClick: (CommentId) -> Unit,
    onJoinTeam: (TeamId) -> Unit,
    onLeaveTeam: (TeamId) -> Unit,
    onVoteCaptain: (TeamId, UserId) -> Unit = { _, _ -> },
    onTransferCaptain: (TeamId, UserId) -> Unit = { _, _ -> },
    onTeamTaskPickSolutionFile: () -> Unit,
    onSubmitTeamSolution: (String) -> Unit,
    onRemovePendingTeamSolutionFile: (String) -> Unit,
    onRemoveSavedTeamSolutionFile: (String) -> Unit,
    onOpenGradeDistribution: () -> Unit,
    onDownloadAttachment: (UUID) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val teamTask: TeamTaskPostState = state.teamTask ?: TeamTaskPostState()
    val teams: List<Team> = teamTask.teams
    val myTeam: Team? = teamTask.myTeam
    val showTeamCarousel: Boolean = myTeam == null && teams.isNotEmpty()
    val publicComments: List<CommentUi> = state.comments.filter { !it.isPrivate }
    val privateComments: List<CommentUi> = teamTask.solutionComments
    val showStudentActions: Boolean = state.currentUserRole == CourseRole.STUDENT

    val pageCount: Int = maxOf(teams.size, 1)
    val pagerState =
        rememberPagerState(
            initialPage = 0,
            pageCount = { pageCount },
        )

    LaunchedEffect(teams, myTeam?.id, pageCount, showTeamCarousel) {
        if (!showTeamCarousel || teams.isEmpty()) return@LaunchedEffect
        val target =
            myTeam?.let { mt ->
                teams.indexOfFirst { it.id == mt.id }.takeIf { it >= 0 }
            } ?: 0
        val safe = target.coerceIn(0, pageCount - 1)
        if (pagerState.currentPage != safe) {
            pagerState.scrollToPage(safe)
        }
    }

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    /** Высота свёрнутого листа; нижний inset для текста — через `navigationBarsPadding` у колонки листа. */
    val sheetPeekHeight = 132.dp
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = sheetPeekHeight,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        sheetShadowElevation = 18.dp,
        sheetTonalElevation = 6.dp,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
                TeamSheetPeekSummary(
                    myTeam = teamTask.myTeam,
                    isLoading = state.isLoadingTeamSection,
                )
                state.teamSectionError?.let { err ->
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
                when {
                    teams.isEmpty() && myTeam == null && !state.isLoadingTeamSection -> {
                        Text(
                            text = "Список команд пуст",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    teams.isEmpty() && myTeam == null && state.isLoadingTeamSection -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    myTeam != null -> {
                        TeamPageCard(
                            post = post,
                            team = myTeam,
                            myTeam = myTeam,
                            currentUserId = teamTask.currentUserId,
                            onJoinTeam = onJoinTeam,
                            onLeaveTeam = onLeaveTeam,
                            onVoteCaptain = onVoteCaptain,
                            onTransferCaptain = onTransferCaptain,
                            showStudentActions = showStudentActions,
                            canLeaveTeam = teamTask.canLeaveTeam,
                            isCaptain = teamTask.isCaptain,
                            showTeamHeader = true,
                        )
                    }
                    else -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(
                                            (pagerState.currentPage - 1).coerceAtLeast(0),
                                        )
                                    }
                                },
                                enabled = pagerState.currentPage > 0,
                            ) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Назад")
                            }
                            Text(
                                text = teams.getOrNull(pagerState.currentPage)?.name.orEmpty(),
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp),
                            )
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(
                                            (pagerState.currentPage + 1).coerceAtMost(pageCount - 1),
                                        )
                                    }
                                },
                                enabled = pagerState.currentPage < pageCount - 1,
                            ) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Вперёд")
                            }
                        }
                        HorizontalPager(
                            state = pagerState,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                        ) { page ->
                            val team: Team? = teams.getOrNull(page)
                            if (team != null) {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight(),
                                    contentAlignment = Alignment.TopCenter,
                                ) {
                                    TeamPageCard(
                                        post = post,
                                        team = team,
                                        myTeam = myTeam,
                                        currentUserId = teamTask.currentUserId,
                                        onJoinTeam = onJoinTeam,
                                        onLeaveTeam = onLeaveTeam,
                                        onVoteCaptain = onVoteCaptain,
                                        onTransferCaptain = onTransferCaptain,
                                        showStudentActions = showStudentActions,
                                        canLeaveTeam = teamTask.canLeaveTeam,
                                        isCaptain = teamTask.isCaptain,
                                        showTeamHeader = false,
                                    )
                                }
                            } else {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "Нет данных о команде",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                }
                if (myTeam != null && teamTask.solution?.score != null && showStudentActions) {
                    Spacer(modifier = Modifier.height(16.dp))
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
                if (myTeam != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))
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
                            text = "Решение команды",
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
                    TeamSolutionBlock(
                        solution = teamTask.solution,
                        pendingFiles = teamTask.pendingSolutionFiles,
                        removedSavedFileIds = teamTask.removedSavedSolutionFileIds,
                        isCaptain = teamTask.isCaptain,
                        showStudentActions = showStudentActions,
                        isDeadlinePassed = isDeadlinePassed,
                        onPickFile = onTeamTaskPickSolutionFile,
                        onSubmit = onSubmitTeamSolution,
                        onRemovePendingFile = onRemovePendingTeamSolutionFile,
                        onRemoveSavedFile = onRemoveSavedTeamSolutionFile,
                        onDownloadAttachment = onDownloadAttachment,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Приватные комментарии",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    teamTask.solutionCommentsError?.let { err ->
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    TextButton(
                        onClick = onOpenPrivateCommentComposer,
                        enabled = teamTask.solution != null,
                        modifier = Modifier.testTag("team_task_private_comment_button"),
                    ) {
                        Text("Написать приватный комментарий")
                    }
                    if (teamTask.solution == null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Доступны после того, как капитан отправит решение.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (privateComments.isEmpty()) {
                        Text(
                            text = "Нет приватных комментариев",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        privateComments.forEach { c ->
                            PostCommentItem(
                                comment = c,
                                onLoadRepliesClick = onLoadRepliesClick,
                                onReplyClick = onReplyClick,
                                loadingRepliesForCommentId = state.loadingRepliesForCommentId,
                                showThreadActions = false,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    val noTeamPlaceholder: String =
                        when {
                            state.isLoadingTeamSection -> "Загрузка…"
                            teams.isEmpty() ->
                                "Решение команды и приватные комментарии станут доступны, когда по заданию появятся команды."
                            else ->
                                "Решение команды и приватные комментарии доступны после вступления в команду. Выберите команду в блоке выше."
                        }
                    Text(
                        text = noTeamPlaceholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
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
                        .testTag("team_task_post_main"),
            ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                PostScreenTeamTaskSection(
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
                    loadingRepliesForCommentId = state.loadingRepliesForCommentId,
                )
            }
            }
        }
    }
}

@Composable
private fun TeamSheetPeekSummary(
    myTeam: Team?,
    isLoading: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        when {
            isLoading ->
                Text(
                    text = "Загрузка…",
                    style = MaterialTheme.typography.bodyLarge,
                )
            myTeam != null ->
                Text(
                    text = "Вы в команде: ${myTeam.name}",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            else ->
                Text(
                    text = "Вы пока не в команде — выберите команду ниже",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
        }
    }
}

@Composable
private fun TeamPageCard(
    post: TeamTaskPost,
    team: Team,
    myTeam: Team?,
    currentUserId: UserId?,
    onJoinTeam: (TeamId) -> Unit,
    onLeaveTeam: (TeamId) -> Unit,
    onVoteCaptain: (TeamId, UserId) -> Unit,
    onTransferCaptain: (TeamId, UserId) -> Unit,
    showStudentActions: Boolean,
    canLeaveTeam: Boolean,
    isCaptain: Boolean,
    /** Заголовок с названием над списком (не внутри рамки). В карусели не показываем — имя уже между стрелками. */
    showTeamHeader: Boolean,
) {
    val maxTeamSize: Int? = post.maxTeamSize
    val maxSize: Int = maxTeamSize ?: Int.MAX_VALUE
    val emptySlots: Int =
        if (maxTeamSize != null) {
            (maxTeamSize - team.members.size).coerceAtLeast(0)
        } else {
            0
        }
    val canJoin: Boolean =
        showStudentActions &&
            (post.allowJoinTeam ?: true) &&
            myTeam == null &&
            team.members.size < maxSize
    val isMyTeam: Boolean = myTeam?.id == team.id
    val hasLeader: Boolean = team.members.any { it.role == TeamMemberRole.LEADER }
    val canVoteCaptain: Boolean =
        post.captainMode == TeamCaptainSelectionMode.VOTING_AND_LOTTERY && !hasLeader
    val canTransferCaptain: Boolean = isCaptain && post.allowStudentTransferCaptain == true

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("team_task_team_card"),
        verticalArrangement = Arrangement.Top,
    ) {
        if (showTeamHeader) {
            Text(
                text = team.name,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        val outlineColor = MaterialTheme.colorScheme.outlineVariant
        val listShape = RoundedCornerShape(16.dp)
        val emptySlotFill = MaterialTheme.colorScheme.surfaceContainerLow
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(listShape)
                    .border(1.dp, outlineColor, listShape),
        ) {
            var isFirstRow = true
            team.members.forEach { m ->
                if (!isFirstRow) {
                    HorizontalDivider(color = outlineColor)
                }
                isFirstRow = false
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = m.credentials,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight =
                            if (currentUserId != null && m.userId == currentUserId) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            },
                        modifier = Modifier.weight(1f),
                    )
                    if (m.role == TeamMemberRole.LEADER) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text = "Капитан",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                    if (showStudentActions && isMyTeam && (canVoteCaptain || canTransferCaptain)) {
                        var isMenuExpanded: Boolean by remember(m.userId) { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { isMenuExpanded = true },
                                modifier =
                                    Modifier
                                        .testTag("team_member_actions_button")
                                        .height(24.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Действия с участником",
                                )
                            }
                            DropdownMenu(
                                expanded = isMenuExpanded,
                                onDismissRequest = { isMenuExpanded = false },
                            ) {
                                if (canVoteCaptain) {
                                    DropdownMenuItem(
                                        text = { Text("Предложить как капитана") },
                                        onClick = {
                                            isMenuExpanded = false
                                            onVoteCaptain(team.id, m.userId)
                                        },
                                    )
                                }
                                if (canTransferCaptain) {
                                    DropdownMenuItem(
                                        text = { Text("Передать роль капитана") },
                                        onClick = {
                                            isMenuExpanded = false
                                            onTransferCaptain(team.id, m.userId)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            repeat(emptySlots) { idx ->
                if (team.members.isNotEmpty() || idx > 0) {
                    HorizontalDivider(color = outlineColor)
                }
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(emptySlotFill)
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                ) {
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (showStudentActions && myTeam == null) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { onJoinTeam(team.id) },
                enabled = canJoin,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("team_task_join_button"),
            ) {
                val joinDisabledByPolicy = post.allowJoinTeam == false
                Text(
                    when {
                        canJoin -> "Присоединиться"
                        joinDisabledByPolicy -> "Вступление отключено"
                        else -> "Команда заполнена"
                    },
                )
            }
        }
        if (showStudentActions && isMyTeam) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { onLeaveTeam(team.id) },
                enabled = canLeaveTeam,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("team_task_leave_button"),
            ) {
                Text("Покинуть команду")
            }
        }
    }
}

@Composable
private fun TeamSolutionBlock(
    solution: TeamTaskSolution?,
    pendingFiles: List<FileInfo>,
    removedSavedFileIds: Set<String>,
    isCaptain: Boolean,
    showStudentActions: Boolean,
    isDeadlinePassed: Boolean,
    onPickFile: () -> Unit,
    onSubmit: (String) -> Unit,
    onRemovePendingFile: (String) -> Unit,
    onRemoveSavedFile: (String) -> Unit,
    onDownloadAttachment: (UUID) -> Unit,
) {
    val canEdit: Boolean = showStudentActions && isCaptain && !isDeadlinePassed
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

    when {
        solution == null && isCaptain && showStudentActions -> {
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
                            onClick = teamSolutionFileDownloadClick(f, onDownloadAttachment),
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
                        .testTag("team_task_submit_solution"),
            ) {
                Text("Отправить решение")
            }
        }
        solution == null && !isCaptain ->
            Text(
                text = "Решение ещё не отправлено капитаном.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        solution != null -> {
            if (isCaptain && showStudentActions) {
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
                            onClick = teamSolutionFileDownloadClick(f, onDownloadAttachment),
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
                            onClick = teamSolutionFileDownloadClick(f, onDownloadAttachment),
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
            if (isCaptain && showStudentActions) {
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
                            .testTag("team_task_save_solution"),
                ) {
                    Text("Сохранить решение")
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

private fun teamSolutionFileDownloadClick(
    f: FileInfo,
    onDownloadAttachment: (UUID) -> Unit,
): (() -> Unit)? {
    val id = runCatching { UUID.fromString(f.id) }.getOrNull() ?: return null
    return { onDownloadAttachment(id) }
}
