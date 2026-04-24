package com.stuf.classroom.post

import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.FileInfo
import com.stuf.domain.model.Solution
import com.stuf.domain.model.Team
import com.stuf.domain.model.TeamTaskSolution
import com.stuf.domain.model.UserId

data class TeamTaskPostState(
    val teams: List<Team> = emptyList(),
    val myTeam: Team? = null,
    val solution: TeamTaskSolution? = null,
    /** Комментарии к решению (`/api/solution/{id}/comment`). */
    val solutionComments: List<CommentUi> = emptyList(),
    val isLoadingSolutionComments: Boolean = false,
    val solutionCommentsError: String? = null,
    val isCaptain: Boolean = false,
    /** Текущий пользователь (для выделения в списке участников). */
    val currentUserId: UserId? = null,
    /** Новые файлы, выбранные капитаном до отправки решения (ещё не на сервере в составе решения). */
    val pendingSolutionFiles: List<FileInfo> = emptyList(),
    /** Id файлов из сохранённого решения, помеченных к удалению до следующей отправки. */
    val removedSavedSolutionFileIds: Set<String> = emptySet(),
    /**
     * Можно ли покинуть команду (кнопка активна).
     * Сейчас: нельзя после дедлайна; позже — доп. правила с бэкенда.
     */
    val canLeaveTeam: Boolean = true,
)

/** Индивидуальное задание ([com.stuf.domain.model.TaskPost]). */
data class IndividualTaskPostState(
    val solution: Solution? = null,
    val isLoadingSolutionSection: Boolean = false,
    val sectionError: String? = null,
    val currentUserId: UserId? = null,
    val pendingSolutionFiles: List<FileInfo> = emptyList(),
    val removedSavedSolutionFileIds: Set<String> = emptySet(),
)

data class PostUiState(
    val isLoadingPost: Boolean = false,
    val postLoadError: String? = null,
    val isLoadingComments: Boolean = false,
    val commentsLoadError: String? = null,
    val isLoadingTeamSection: Boolean = false,
    val teamSectionError: String? = null,
    /** Подгрузка ответов для комментария (id комментария). */
    val loadingRepliesForCommentId: String? = null,
    val content: PostScreenContent? = null,
    val comments: List<CommentUi> = emptyList(),
    val currentUserId: UserId? = null,
    val currentUserName: String? = null,
    val currentUserRole: CourseRole = CourseRole.STUDENT,
    /** Данные командного задания; заполняется только для [PostScreenContent.TeamTask]. */
    val teamTask: TeamTaskPostState? = null,
    /** Индивидуальное задание; только [PostScreenContent.Task]. */
    val individualTask: IndividualTaskPostState? = null,
)
