package com.stuf.data.demo

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.AnonymizedSolution
import com.stuf.domain.model.FileInfo
import com.stuf.domain.model.GradingMode
import com.stuf.domain.model.PeerReviewId
import com.stuf.domain.model.PeerReviewProgress
import com.stuf.domain.model.PeerReviewTarget
import com.stuf.domain.model.PeerReviewTeamTarget
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.Solution
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.TaskPost
import com.stuf.domain.model.Team
import com.stuf.domain.model.TeamId
import com.stuf.domain.model.TeamMember
import com.stuf.domain.model.TeamMemberRole
import com.stuf.domain.model.TeamTaskPost
import com.stuf.domain.model.TeamTaskSolution
import com.stuf.domain.model.UserId
import com.stuf.domain.model.UserRef
import com.stuf.grading.domain.model.SelfAssessmentDraft
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.random.Random

/**
 * In-memory P2P-симулятор для offline-демо. Логика повторяет [com.stuf.domain.bdd.support.BddBackend].
 */
internal object DemoPeerReviewSupport {
    private val peerPoolByTask = mutableMapOf<TaskId, MutableList<Solution>>()
    private val peerTeamSolutionsByTask = mutableMapOf<TaskId, MutableList<TeamTaskSolution>>()
    private val peerReviews = mutableMapOf<PeerReviewId, DemoPeerReviewRecord>()
    private val solutionCounted = mutableMapOf<SolutionId, Boolean>()

    fun clear() {
        peerPoolByTask.clear()
        peerTeamSolutionsByTask.clear()
        peerReviews.clear()
        solutionCounted.clear()
    }

    fun findTaskIdByReviewId(reviewId: PeerReviewId): TaskId? = peerReviews[reviewId]?.taskId

    fun findTaskIdByPeerTeamSolutionId(teamSolutionId: SolutionId): TaskId? =
        peerTeamSolutionsByTask.entries.firstOrNull { (_, list) ->
            list.any { it.id == teamSolutionId }
        }?.key

    fun seedIndividualP2p(now: OffsetDateTime) {
        val taskId = TaskId(DemoIds.postMobileP2p.value)
        peerPoolByTask[taskId] =
            mutableListOf(
                Solution(
                    id = DemoIds.solutionPeerAlexP2p,
                    taskId = taskId,
                    authorId = DemoIds.userPeerAlex,
                    text = "Quick sort: pivot в середине, O(n log n) в среднем.",
                    files =
                        listOf(
                            FileInfo(
                                id = DemoIds.filePeerAlexP2pPdf.toString(),
                                name = "quick-sort-draft.pdf",
                            ),
                        ),
                    score = null,
                    status = SolutionStatus.PENDING,
                    updatedAt = now.minusHours(5),
                ),
                Solution(
                    id = DemoIds.solutionPeerMariaP2p,
                    taskId = taskId,
                    authorId = DemoIds.userPeerMaria,
                    text = "Merge sort — стабильная сортировка, O(n log n) гарантированно.",
                    files =
                        listOf(
                            FileInfo(
                                id = DemoIds.filePeerMariaP2pPdf.toString(),
                                name = "merge-sort-notes.pdf",
                            ),
                        ),
                    score = null,
                    status = SolutionStatus.PENDING,
                    updatedAt = now.minusHours(4),
                ),
                Solution(
                    id = DemoIds.solutionPeerOlegP2p,
                    taskId = taskId,
                    authorId = DemoIds.userPeerOleg,
                    text = "Heap sort in-place, но константа хуже merge sort.",
                    files =
                        listOf(
                            FileInfo(
                                id = DemoIds.filePeerOlegP2pPdf.toString(),
                                name = "heap-sort-comparison.pdf",
                            ),
                        ),
                    score = null,
                    status = SolutionStatus.PENDING,
                    updatedAt = now.minusHours(3),
                ),
            )
    }

    fun seedTeamP2p(now: OffsetDateTime): TeamP2pSeed {
        val taskId = TaskId(DemoIds.postTeamMobileP2p.value)
        val alphaTeam =
            Team(
                id = DemoIds.teamMobileP2pAlpha,
                name = "Alpha (ваша команда)",
                members =
                    listOf(
                        TeamMember(DemoIds.userStudent, "Студент Демо", TeamMemberRole.LEADER),
                        TeamMember(DemoIds.userPeerAlex, "Алексей К.", TeamMemberRole.MEMBER),
                    ),
            )
        val betaTeam =
            Team(
                id = DemoIds.teamMobileP2pBeta,
                name = "Beta",
                members =
                    listOf(
                        TeamMember(DemoIds.userPeerMaria, "Мария С.", TeamMemberRole.LEADER),
                        TeamMember(DemoIds.userPeerOleg, "Олег В.", TeamMemberRole.MEMBER),
                    ),
            )
        val gammaTeam =
            Team(
                id = DemoIds.teamMobileP2pGamma,
                name = "Gamma",
                members =
                    listOf(
                        TeamMember(DemoIds.userPeerAnna, "Анна Л.", TeamMemberRole.LEADER),
                        TeamMember(DemoIds.userTeacher, "Учитель Иванова", TeamMemberRole.MEMBER),
                    ),
            )
        peerTeamSolutionsByTask[taskId] =
            mutableListOf(
                TeamTaskSolution(
                    id = DemoIds.solutionTeamMobileP2pBeta,
                    taskId = taskId,
                    text = "Beta: навигация через NavHost, bottom bar.",
                    files = emptyList(),
                    score = null,
                    status = SolutionStatus.PENDING,
                    updatedAt = now.minusHours(2),
                    team = betaTeam,
                    submittedBy = UserRef(DemoIds.userPeerMaria, "Мария С."),
                ),
                TeamTaskSolution(
                    id = DemoIds.solutionTeamMobileP2pGamma,
                    taskId = taskId,
                    text = "Gamma: список курсов с pull-to-refresh.",
                    files = emptyList(),
                    score = null,
                    status = SolutionStatus.PENDING,
                    updatedAt = now.minusHours(1),
                    team = gammaTeam,
                    submittedBy = UserRef(DemoIds.userPeerAnna, "Анна Л."),
                ),
            )
        val myTeamSolution =
            TeamTaskSolution(
                id = DemoIds.solutionTeamMobileP2pAlpha,
                taskId = taskId,
                text = "Alpha: экран списка на Compose с фильтрацией.",
                files = emptyList(),
                score = null,
                status = SolutionStatus.PENDING,
                updatedAt = now,
                team = alphaTeam,
                submittedBy = UserRef(DemoIds.userStudent, "Студент Демо"),
            )
        return TeamP2pSeed(
            myTeam = alphaTeam,
            allTeams = listOf(alphaTeam, betaTeam, gammaTeam),
            myTeamSolution = myTeamSolution,
            peerSolutions = peerTeamSolutionsByTask[taskId]!!,
        )
    }

    data class TeamP2pSeed(
        val myTeam: Team,
        val allTeams: List<Team>,
        val myTeamSolution: TeamTaskSolution,
        val peerSolutions: List<TeamTaskSolution>,
    )

    fun enrichIndividualSolution(
        sol: Solution,
        post: TaskPost?,
    ): Solution {
        if (post?.gradingMode != GradingMode.PEER_TO_PEER) return sol
        val progress = buildIndividualProgress(post, sol.authorId, sol.id)
        return sol.copy(peerReviewProgress = progress)
    }

    fun enrichTeamSolution(
        sol: TeamTaskSolution,
        post: TeamTaskPost?,
        userId: UserId,
    ): TeamTaskSolution {
        if (post?.gradingMode != GradingMode.PEER_TO_PEER) return sol
        return sol.copy(peerReviewProgress = buildTeamProgress(post, userId, sol.team.id))
    }

    fun getNextPeerReview(
        taskId: TaskId,
        post: TaskPost?,
        studentSolution: Solution?,
    ): DomainResult<PeerReviewTarget?> {
        if (post == null || post.gradingMode != GradingMode.PEER_TO_PEER) {
            return failure(DomainError.Validation("P2P недоступен в режиме TeacherReview"))
        }
        if (studentSolution == null) {
            return failure(DomainError.Validation("Сначала прикрепите своё решение"))
        }
        val userId = DemoIds.userStudent
        val existing =
            peerReviews.values.firstOrNull {
                it.reviewerId == userId && it.taskId == taskId && it.status == DemoPeerReviewStatus.ASSIGNED
            }
        if (existing != null) {
            return DomainResult.Success(existing.toTarget(post, taskId))
        }
        val candidates =
            allIndividualSolutions(taskId, studentSolution)
                .filter { (_, authorId) -> authorId != userId }
                .map { it.second }
                .filter { authorId ->
                    peerReviews.values.none {
                        it.reviewerId == userId &&
                            it.solutionAuthorId == authorId &&
                            it.taskId == taskId
                    }
                }
        if (candidates.isEmpty()) {
            return DomainResult.Success(null)
        }
        val authorId = candidates[Random.nextInt(candidates.size)]
        val solution = findIndividualSolution(taskId, authorId, studentSolution)!!
        val record =
            DemoPeerReviewRecord(
                id = PeerReviewId(UUID.randomUUID()),
                taskId = taskId,
                reviewerId = userId,
                solutionId = solution.id,
                solutionAuthorId = authorId,
                status = DemoPeerReviewStatus.ASSIGNED,
                assignedAt = OffsetDateTime.now(),
                completedAt = null,
                reviewerTeamId = null,
                teamSolutionId = null,
            )
        peerReviews[record.id] = record
        return DomainResult.Success(record.toTarget(post, taskId))
    }

    fun submitPeerReview(
        reviewId: PeerReviewId,
        draft: SelfAssessmentDraft,
        post: TaskPost?,
    ): DomainResult<PeerReviewProgress> {
        val record = peerReviews[reviewId] ?: return failure(DomainError.NotFound)
        if (record.reviewerId != DemoIds.userStudent) {
            return failure(DomainError.Forbidden)
        }
        if (record.status == DemoPeerReviewStatus.COMPLETED) {
            return failure(DomainError.Validation("PeerReview уже завершён"))
        }
        record.status = DemoPeerReviewStatus.COMPLETED
        record.completedAt = OffsetDateTime.now()
        val task = post ?: return failure(DomainError.NotFound)
        return DomainResult.Success(buildIndividualProgress(task, DemoIds.userStudent, record.solutionId))
    }

    fun getIndividualProgress(
        taskId: TaskId,
        post: TaskPost?,
        studentSolution: Solution?,
    ): DomainResult<PeerReviewProgress> {
        val task = post ?: return failure(DomainError.NotFound)
        val solutionId = studentSolution?.id
        return DomainResult.Success(buildIndividualProgress(task, DemoIds.userStudent, solutionId))
    }

    fun finishIndividualPeerReview(
        taskId: TaskId,
        post: TaskPost?,
        studentSolution: Solution?,
    ): DomainResult<PeerReviewProgress> {
        val task = post ?: return failure(DomainError.NotFound)
        val progress = buildIndividualProgress(task, DemoIds.userStudent, studentSolution?.id)
        if (!progress.canFinish) {
            return failure(DomainError.Validation("Не выполнен минимум оцениваний"))
        }
        val solution = studentSolution
            ?: return failure(DomainError.Validation("Нет прикреплённого решения"))
        solutionCounted[solution.id] = true
        return DomainResult.Success(progress.copy(isCounted = true))
    }

    fun getAvailableTeamPeerReviews(
        taskId: TaskId,
        post: TeamTaskPost?,
        myTeam: Team?,
    ): DomainResult<List<PeerReviewTeamTarget>> {
        if (post == null) return failure(DomainError.NotFound)
        if (post.gradingMode != GradingMode.PEER_TO_PEER) {
            return failure(DomainError.Validation("P2P недоступен в режиме TeacherReview"))
        }
        val myTeamId = myTeam?.id ?: return failure(DomainError.Validation("Студент не в команде"))
        val userId = DemoIds.userStudent
        val targets =
            peerTeamSolutionsByTask[taskId].orEmpty()
                .filter { it.team.id != myTeamId }
                .map { sol ->
                    val reviewed =
                        peerReviews.values.any {
                            it.reviewerId == userId &&
                                it.teamSolutionId == sol.id &&
                                it.status == DemoPeerReviewStatus.COMPLETED
                        }
                    PeerReviewTeamTarget(
                        teamSolutionId = checkNotNull(sol.id),
                        teamName = sol.team.name,
                        submittedAt = sol.updatedAt,
                        alreadyReviewed = reviewed,
                    )
                }
        return DomainResult.Success(targets)
    }

    fun submitTeamPeerReview(
        taskId: TaskId,
        teamSolutionId: SolutionId,
        draft: SelfAssessmentDraft,
        post: TeamTaskPost?,
        myTeam: Team?,
    ): DomainResult<PeerReviewProgress> {
        val task = post ?: return failure(DomainError.NotFound)
        if (task.gradingMode != GradingMode.PEER_TO_PEER) {
            return failure(DomainError.Validation("P2P недоступен в режиме TeacherReview"))
        }
        val reviewerTeamId = myTeam?.id ?: return failure(DomainError.Forbidden)
        val userId = DemoIds.userStudent
        val targetSolution =
            peerTeamSolutionsByTask[taskId]?.find { it.id == teamSolutionId }
                ?: return failure(DomainError.NotFound)
        if (targetSolution.team.id == reviewerTeamId) {
            return failure(DomainError.Validation("Нельзя оценить решение своей команды"))
        }
        val alreadyExists =
            peerReviews.values.any {
                it.reviewerId == userId &&
                    it.teamSolutionId == teamSolutionId &&
                    it.status == DemoPeerReviewStatus.COMPLETED
            }
        if (alreadyExists) {
            return failure(DomainError.Validation("EntryExists"))
        }
        val record =
            DemoPeerReviewRecord(
                id = PeerReviewId(UUID.randomUUID()),
                taskId = taskId,
                reviewerId = userId,
                solutionId = null,
                solutionAuthorId = null,
                status = DemoPeerReviewStatus.COMPLETED,
                assignedAt = OffsetDateTime.now(),
                completedAt = OffsetDateTime.now(),
                reviewerTeamId = reviewerTeamId,
                teamSolutionId = teamSolutionId,
            )
        peerReviews[record.id] = record
        return DomainResult.Success(buildTeamProgress(task, userId, reviewerTeamId))
    }

    fun getTeamPeerReviewProgress(
        taskId: TaskId,
        post: TeamTaskPost?,
        myTeam: Team?,
    ): DomainResult<PeerReviewProgress> {
        val task = post ?: return failure(DomainError.NotFound)
        val teamId = myTeam?.id
        return DomainResult.Success(buildTeamProgress(task, DemoIds.userStudent, teamId))
    }

    private fun allIndividualSolutions(
        taskId: TaskId,
        studentSolution: Solution?,
    ): List<Pair<Solution, UserId>> {
        val pool = peerPoolByTask[taskId].orEmpty().map { it to it.authorId }
        val student =
            studentSolution?.let { sol ->
                if (sol.taskId == taskId) listOf(sol to sol.authorId) else emptyList()
            }.orEmpty()
        return student + pool
    }

    private fun findIndividualSolution(
        taskId: TaskId,
        authorId: UserId,
        studentSolution: Solution?,
    ): Solution? {
        if (studentSolution?.authorId == authorId && studentSolution.taskId == taskId) {
            return studentSolution
        }
        return peerPoolByTask[taskId]?.find { it.authorId == authorId }
    }

    private fun completedReviewsCount(
        userId: UserId,
        taskId: TaskId,
    ): Int =
        peerReviews.values.count {
            it.reviewerId == userId && it.taskId == taskId && it.status == DemoPeerReviewStatus.COMPLETED
        }

    private fun buildIndividualProgress(
        task: TaskPost,
        userId: UserId,
        solutionId: SolutionId?,
    ): PeerReviewProgress {
        val required = task.minPeerReviewsRequired ?: 1
        val completed = completedReviewsCount(userId, TaskId(task.id.value))
        val isCounted = solutionId?.let { solutionCounted[it] == true } ?: false
        return PeerReviewProgress(
            required = required,
            completed = completed,
            canFinish = completed >= required,
            isCounted = isCounted,
        )
    }

    private fun buildTeamProgress(
        task: TeamTaskPost,
        userId: UserId,
        teamId: TeamId?,
    ): PeerReviewProgress {
        val taskId = TaskId(task.id.value)
        val hasTeamSolution = teamId != null && hasOwnTeamSolution(taskId, teamId)
        val completed =
            peerReviews.values.count {
                it.reviewerId == userId && it.taskId == taskId && it.status == DemoPeerReviewStatus.COMPLETED
            }
        val isCounted = hasTeamSolution && completed >= 1
        return PeerReviewProgress(
            required = 1,
            completed = completed,
            canFinish = completed >= 1,
            isCounted = isCounted,
        )
    }

    private fun hasOwnTeamSolution(
        taskId: TaskId,
        teamId: TeamId,
    ): Boolean {
        if (taskId.value == DemoIds.postTeamMobileP2p.value && teamId == DemoIds.teamMobileP2pAlpha) {
            return true
        }
        return false
    }

    private fun DemoPeerReviewRecord.toTarget(
        task: TaskPost,
        taskId: TaskId,
    ): PeerReviewTarget {
        val solution = findIndividualSolution(taskId, checkNotNull(solutionAuthorId), null)!!
        return PeerReviewTarget(
            reviewId = id,
            taskId = taskId,
            solution = AnonymizedSolution(solution.text, solution.files),
            criteria = task.gradingRubric?.criteria.orEmpty(),
            assignedAt = assignedAt,
        )
    }

    private fun <T> failure(error: DomainError): DomainResult<T> = DomainResult.Failure(error)
}
