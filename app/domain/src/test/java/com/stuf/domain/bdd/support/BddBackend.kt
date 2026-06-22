package com.stuf.domain.bdd.support

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.AnonymizedSolution
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.FileInfo
import com.stuf.domain.model.GradingMode
import com.stuf.domain.model.PeerReviewId
import com.stuf.domain.model.PeerReviewProgress
import com.stuf.domain.model.PeerReviewTarget
import com.stuf.domain.model.PeerReviewTeamTarget
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.Review
import com.stuf.domain.model.Score
import com.stuf.domain.model.Solution
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.model.TaskDetails
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
import com.stuf.grading.domain.model.CriterionDefinition
import com.stuf.grading.domain.model.CriterionId
import com.stuf.grading.domain.model.SelfAssessmentDraft
import com.stuf.grading.domain.model.TaskGradingRubric
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.random.Random

/**
 * In-memory симуляция P2P-правил из specs.md / bdd.md для acceptance-тестов.
 */
class BddBackend {
    var courseId: CourseId = CourseId(UUID.randomUUID())
    var courseName: String = "Алгоритмы"
    var currentStudentName: String? = null
    var teacherLoggedIn: Boolean = false

    val posts: MutableMap<PostId, Post> = linkedMapOf()
    val students: MutableMap<String, UserId> = linkedMapOf()
    val teams: MutableMap<String, TeamId> = linkedMapOf()
    val teamMembers: MutableMap<TeamId, MutableSet<UserId>> = linkedMapOf()
    val userTeam: MutableMap<UserId, TeamId> = linkedMapOf()

    val individualSolutions: MutableMap<Pair<TaskId, UserId>, Solution> = linkedMapOf()
    val teamSolutions: MutableMap<Pair<TaskId, TeamId>, TeamTaskSolution> = linkedMapOf()
    val teacherReviewQueue: MutableSet<SolutionId> = linkedSetOf()
    val solutionCounted: MutableMap<SolutionId, Boolean> = linkedMapOf()

    val peerReviews: MutableMap<PeerReviewId, PeerReviewRecord> = linkedMapOf()
    var lastPeerReviewCreated: PeerReviewRecord? = null
    var lastPeerReviewTarget: PeerReviewTarget? = null
    var lastTeamPeerReview: PeerReviewRecord? = null

    var draftGradingMode: GradingMode? = null
    var draftMinPeerReviews: Int? = null
    var draftIsTeam: Boolean = false
    var draftTitle: String = "Задание"
    var explicitDefaultGradingMode: Boolean = false

    var currentTaskTitle: String? = null
    var lastError: DomainError? = null
    var lastProgress: PeerReviewProgress? = null
    var lastSolution: Solution? = null
    var lastTeamSolution: TeamTaskSolution? = null
    var lastAvailableTeamTargets: List<PeerReviewTeamTarget> = emptyList()
    var peerReviewCountBeforeLastRequest: Int = 0

    private val rubric: TaskGradingRubric =
        TaskGradingRubric(
            taskId = "task",
            title = "Рубрика",
            assignmentMaxScore = 10.0,
            criteria =
                listOf(
                    CriterionDefinition.Weighted(
                        id = CriterionId("c1"),
                        title = "Качество",
                        maxScore = 10.0,
                        weight = 1.0,
                    ),
                ),
            studentScoreWeight = 0.5,
        )

    fun reset() {
        courseId = CourseId(UUID.randomUUID())
        courseName = "Алгоритмы"
        currentStudentName = null
        teacherLoggedIn = false
        posts.clear()
        students.clear()
        teams.clear()
        teamMembers.clear()
        userTeam.clear()
        individualSolutions.clear()
        teamSolutions.clear()
        teacherReviewQueue.clear()
        solutionCounted.clear()
        peerReviews.clear()
        lastPeerReviewCreated = null
        lastPeerReviewTarget = null
        lastTeamPeerReview = null
        draftGradingMode = null
        draftMinPeerReviews = null
        draftIsTeam = false
        draftTitle = "Задание"
        explicitDefaultGradingMode = false
        currentTaskTitle = null
        lastError = null
        lastProgress = null
        lastSolution = null
        lastTeamSolution = null
        lastAvailableTeamTargets = emptyList()
        peerReviewCountBeforeLastRequest = 0
    }

    fun loginTeacher(course: String) {
        teacherLoggedIn = true
        courseName = course
    }

    fun loginStudent(name: String) {
        currentStudentName = name
        students.getOrPut(name) { UserId(UUID.randomUUID()) }
    }

    fun joinTeam(studentName: String, teamName: String) {
        val userId = students.getOrPut(studentName) { UserId(UUID.randomUUID()) }
        val teamId = teams.getOrPut(teamName) { TeamId(UUID.randomUUID()) }
        teamMembers.getOrPut(teamId) { linkedSetOf() }.add(userId)
        userTeam[userId] = teamId
    }

    fun registerIndividualTask(
        title: String,
        gradingMode: GradingMode,
        minRequired: Int?,
    ): TaskPost {
        val post =
            TaskPost(
                id = PostId(UUID.randomUUID()),
                courseId = courseId,
                title = title,
                text = title,
                createdAt = OffsetDateTime.now(),
                taskDetails = TaskDetails(deadline = null, isMandatory = true, maxScore = 10),
                gradingMode = gradingMode,
                minPeerReviewsRequired = minRequired,
                gradingRubric = rubric,
            )
        posts[post.id] = post
        currentTaskTitle = title
        return post
    }

    fun registerTeamTask(title: String, gradingMode: GradingMode): TeamTaskPost {
        val post =
            TeamTaskPost(
                id = PostId(UUID.randomUUID()),
                courseId = courseId,
                title = title,
                text = title,
                createdAt = OffsetDateTime.now(),
                taskDetails = TaskDetails(deadline = null, isMandatory = true, maxScore = 10),
                gradingMode = gradingMode,
                gradingRubric = rubric,
            )
        posts[post.id] = post
        currentTaskTitle = title
        return post
    }

    fun createAssignment(): DomainResult<Post> {
        if (!teacherLoggedIn) {
            return failure(DomainError.Unauthorized)
        }
        val mode = if (explicitDefaultGradingMode) GradingMode.TEACHER_REVIEW else (draftGradingMode ?: GradingMode.TEACHER_REVIEW)
        if (!draftIsTeam && mode == GradingMode.PEER_TO_PEER) {
            val min = draftMinPeerReviews
            if (min == null) {
                return failure(
                    DomainError.Validation(
                        "MinPeerReviewsRequired обязателен и должен быть >= 1",
                    ),
                )
            }
            if (min < 1) {
                return failure(DomainError.Validation("MinPeerReviewsRequired должен быть >= 1"))
            }
        }
        val post: Post =
            if (draftIsTeam) {
                TeamTaskPost(
                    id = PostId(UUID.randomUUID()),
                    courseId = courseId,
                    title = draftTitle,
                    text = draftTitle,
                    createdAt = OffsetDateTime.now(),
                    taskDetails = TaskDetails(deadline = null, isMandatory = true, maxScore = 10),
                    gradingMode = mode,
                    gradingRubric = rubric,
                )
            } else {
                TaskPost(
                    id = PostId(UUID.randomUUID()),
                    courseId = courseId,
                    title = draftTitle,
                    text = draftTitle,
                    createdAt = OffsetDateTime.now(),
                    taskDetails = TaskDetails(deadline = null, isMandatory = true, maxScore = 10),
                    gradingMode = mode,
                    minPeerReviewsRequired = if (mode == GradingMode.PEER_TO_PEER) draftMinPeerReviews else null,
                    gradingRubric = rubric,
                )
            }
        posts[post.id] = post
        return DomainResult.Success(post)
    }

    fun updateGradingMode(newMode: GradingMode): DomainResult<Post> {
        val task = findCurrentTask() ?: return failure(DomainError.NotFound)
        if (hasAnySolutions(task)) {
            return failure(DomainError.Validation("Нельзя сменить режим оценивания после появления решений"))
        }
        val updated =
            when (task) {
                is TaskPost -> task.copy(gradingMode = newMode)
                is TeamTaskPost -> task.copy(gradingMode = newMode)
                else -> return failure(DomainError.Validation("Not a task"))
            }
        posts[updated.id] = updated
        return DomainResult.Success(updated)
    }

    fun submitIndividualSolution(studentName: String, taskTitle: String? = null): DomainResult<Solution> {
        val userId = students.getOrPut(studentName) { UserId(UUID.randomUUID()) }
        val task = findTask(taskTitle ?: currentTaskTitle) as? TaskPost
            ?: return failure(DomainError.NotFound)
        if (task.gradingMode == GradingMode.TEACHER_REVIEW) {
            teacherReviewQueue.add(SolutionId(UUID.randomUUID()))
        }
        val solution =
            Solution(
                id = SolutionId(UUID.randomUUID()),
                taskId = TaskId(task.id.value),
                authorId = userId,
                text = "solution",
                files = emptyList(),
                score = null,
                status = SolutionStatus.PENDING,
                updatedAt = OffsetDateTime.now(),
                peerReviewProgress =
                    if (task.gradingMode == GradingMode.PEER_TO_PEER) {
                        buildIndividualProgress(task, userId)
                    } else {
                        null
                    },
            )
        individualSolutions[TaskId(task.id.value) to userId] = solution
        lastSolution = solution
        return DomainResult.Success(solution)
    }

    fun getNextPeerReview(studentName: String, taskTitle: String? = null): DomainResult<PeerReviewTarget?> {
        val userId = students.getOrPut(studentName) { UserId(UUID.randomUUID()) }
        val task = findTask(taskTitle ?: currentTaskTitle) as? TaskPost
            ?: return failure(DomainError.NotFound)
        if (task.gradingMode != GradingMode.PEER_TO_PEER) {
            return failure(DomainError.Validation("P2P недоступен в режиме TeacherReview"))
        }
        val taskId = TaskId(task.id.value)
        if (individualSolutions[taskId to userId] == null) {
            return failure(DomainError.Validation("Сначала прикрепите своё решение"))
        }
        val existing =
            peerReviews.values.firstOrNull {
                it.reviewerId == userId && it.taskId == taskId && it.status == PeerReviewStatus.ASSIGNED
            }
        if (existing != null) {
            val target = existing.toTarget(task)
            lastPeerReviewTarget = target
            return DomainResult.Success(target)
        }
        peerReviewCountBeforeLastRequest = peerReviews.size
        val candidates =
            individualSolutions
                .filter { (key, _) -> key.first == taskId && key.second != userId }
                .keys
                .map { it.second }
                .filter { authorId ->
                    peerReviews.values.none {
                        it.reviewerId == userId &&
                            it.solutionAuthorId == authorId &&
                            it.taskId == taskId
                    }
                }
        if (candidates.isEmpty()) {
            lastPeerReviewTarget = null
            return DomainResult.Success(null)
        }
        val authorId = candidates[Random.nextInt(candidates.size)]
        val solution = individualSolutions[taskId to authorId]!!
        val record =
            PeerReviewRecord(
                id = PeerReviewId(UUID.randomUUID()),
                taskId = taskId,
                reviewerId = userId,
                solutionId = solution.id,
                solutionAuthorId = authorId,
                status = PeerReviewStatus.ASSIGNED,
                assignedAt = OffsetDateTime.now(),
                completedAt = null,
                reviewerTeamId = null,
                teamSolutionId = null,
            )
        peerReviews[record.id] = record
        lastPeerReviewCreated = record
        val target = record.toTarget(task)
        lastPeerReviewTarget = target
        return DomainResult.Success(target)
    }

    fun submitPeerReview(
        studentName: String,
        reviewId: PeerReviewId,
        draft: SelfAssessmentDraft = SelfAssessmentDraft(),
    ): DomainResult<PeerReviewProgress> {
        val userId = students.getOrPut(studentName) { UserId(UUID.randomUUID()) }
        val record = peerReviews[reviewId] ?: return failure(DomainError.NotFound)
        if (record.reviewerId != userId) {
            return failure(DomainError.Forbidden)
        }
        if (record.status == PeerReviewStatus.COMPLETED) {
            return failure(DomainError.Validation("PeerReview уже завершён"))
        }
        record.status = PeerReviewStatus.COMPLETED
        record.completedAt = OffsetDateTime.now()
        val task = findTaskById(record.taskId) as? TaskPost ?: return failure(DomainError.NotFound)
        val progress = buildIndividualProgress(task, userId)
        lastProgress = progress
        return DomainResult.Success(progress)
    }

    fun finishPeerReview(studentName: String, taskTitle: String? = null): DomainResult<PeerReviewProgress> {
        val userId = students.getOrPut(studentName) { UserId(UUID.randomUUID()) }
        val task = findTask(taskTitle ?: currentTaskTitle) as? TaskPost
            ?: return failure(DomainError.NotFound)
        val progress = buildIndividualProgress(task, userId)
        if (!progress.canFinish) {
            lastProgress = progress
            return failure(DomainError.Validation("Не выполнен минимум оцениваний"))
        }
        val solution = individualSolutions[TaskId(task.id.value) to userId]
            ?: return failure(DomainError.Validation("Нет прикреплённого решения"))
        solutionCounted[solution.id] = true
        val counted = progress.copy(isCounted = true)
        individualSolutions[TaskId(task.id.value) to userId] =
            solution.copy(peerReviewProgress = counted)
        lastProgress = counted
        return DomainResult.Success(counted)
    }

    fun getIndividualProgress(studentName: String, taskTitle: String? = null): DomainResult<PeerReviewProgress> {
        val userId = students.getOrPut(studentName) { UserId(UUID.randomUUID()) }
        val task = findTask(taskTitle ?: currentTaskTitle) as? TaskPost
            ?: return failure(DomainError.NotFound)
        val progress = buildIndividualProgress(task, userId)
        lastProgress = progress
        return DomainResult.Success(progress)
    }

    fun getAvailableTeamReviews(studentName: String, taskTitle: String? = null): DomainResult<List<PeerReviewTeamTarget>> {
        val userId = students.getOrPut(studentName) { UserId(UUID.randomUUID()) }
        val teamId = userTeam[userId] ?: return failure(DomainError.Validation("Студент не в команде"))
        val task = findTask(taskTitle ?: currentTaskTitle) as? TeamTaskPost
            ?: return failure(DomainError.NotFound)
        val taskId = TaskId(task.id.value)
        val targets =
            teamSolutions
                .filter { (key, sol) -> key.first == taskId && key.second != teamId }
                .map { (key, sol) ->
                    val reviewed =
                        peerReviews.values.any {
                            it.reviewerId == userId &&
                                it.teamSolutionId == sol.id &&
                                it.status == PeerReviewStatus.COMPLETED
                        }
                    PeerReviewTeamTarget(
                        teamSolutionId = sol.id ?: SolutionId(UUID.randomUUID()),
                        teamName = teamNameById(key.second),
                        submittedAt = sol.updatedAt,
                        alreadyReviewed = reviewed,
                    )
                }
        lastAvailableTeamTargets = targets
        return DomainResult.Success(targets)
    }

    fun submitTeamPeerReview(
        studentName: String,
        teamName: String,
        draft: SelfAssessmentDraft = SelfAssessmentDraft(),
    ): DomainResult<PeerReviewProgress> {
        val userId = students.getOrPut(studentName) { UserId(UUID.randomUUID()) }
        val reviewerTeamId = userTeam[userId] ?: return failure(DomainError.Forbidden)
        val targetTeamId = teams[teamName] ?: return failure(DomainError.NotFound)
        if (reviewerTeamId == targetTeamId) {
            return failure(DomainError.Validation("Нельзя оценить решение своей команды"))
        }
        val task = findTask(currentTaskTitle) as? TeamTaskPost
            ?: return failure(DomainError.NotFound)
        val taskId = TaskId(task.id.value)
        val teamSolution = teamSolutions[taskId to targetTeamId]
            ?: return failure(DomainError.NotFound)
        val alreadyExists =
            peerReviews.values.any {
                it.reviewerId == userId &&
                    it.teamSolutionId == teamSolution.id &&
                    it.status == PeerReviewStatus.COMPLETED
            }
        if (alreadyExists) {
            return failure(DomainError.Validation("EntryExists"))
        }
        val record =
            PeerReviewRecord(
                id = PeerReviewId(UUID.randomUUID()),
                taskId = taskId,
                reviewerId = userId,
                solutionId = null,
                solutionAuthorId = null,
                status = PeerReviewStatus.COMPLETED,
                assignedAt = OffsetDateTime.now(),
                completedAt = OffsetDateTime.now(),
                reviewerTeamId = reviewerTeamId,
                teamSolutionId = teamSolution.id,
            )
        peerReviews[record.id] = record
        lastTeamPeerReview = record
        val progress = buildTeamProgress(task, userId)
        lastProgress = progress
        return DomainResult.Success(progress)
    }

    fun getTeamProgress(studentName: String, taskTitle: String? = null): DomainResult<PeerReviewProgress> {
        val userId = students.getOrPut(studentName) { UserId(UUID.randomUUID()) }
        val task = findTask(taskTitle ?: currentTaskTitle) as? TeamTaskPost
            ?: return failure(DomainError.NotFound)
        val progress = buildTeamProgress(task, userId)
        lastProgress = progress
        return DomainResult.Success(progress)
    }

    fun submitTeamSolution(teamName: String, taskTitle: String? = null): DomainResult<TeamTaskSolution> {
        val teamId = teams.getOrPut(teamName) { TeamId(UUID.randomUUID()) }
        val task = findTask(taskTitle ?: currentTaskTitle) as? TeamTaskPost
            ?: return failure(DomainError.NotFound)
        val member = teamMembers[teamId]?.firstOrNull() ?: students.values.first()
        val solution =
            TeamTaskSolution(
                id = SolutionId(UUID.randomUUID()),
                taskId = TaskId(task.id.value),
                text = "team solution",
                files = emptyList(),
                score = null,
                status = SolutionStatus.PENDING,
                updatedAt = OffsetDateTime.now(),
                team = teamDto(teamId, teamName),
                submittedBy = UserRef(member, ""),
                peerReviewProgress =
                    if (task.gradingMode == GradingMode.PEER_TO_PEER) {
                        buildTeamProgress(task, member)
                    } else {
                        null
                    },
            )
        teamSolutions[TaskId(task.id.value) to teamId] = solution
        lastTeamSolution = solution
        return DomainResult.Success(solution)
    }

    fun getUserSolution(studentName: String, taskTitle: String? = null): DomainResult<Solution?> {
        val userId = students.getOrPut(studentName) { UserId(UUID.randomUUID()) }
        val task = findTask(taskTitle ?: currentTaskTitle) as? TaskPost
            ?: return failure(DomainError.NotFound)
        val taskId = TaskId(task.id.value)
        val solution = individualSolutions[taskId to userId]
        if (solution != null) {
            val updated =
                solution.copy(
                    peerReviewProgress =
                        if (task.gradingMode == GradingMode.PEER_TO_PEER) {
                            buildIndividualProgress(task, userId)
                        } else {
                            null
                        },
                )
            individualSolutions[taskId to userId] = updated
            lastSolution = updated
            return DomainResult.Success(updated)
        }
        lastSolution = null
        return DomainResult.Success(null)
    }

    fun getTeamSolutionForStudent(studentName: String, taskTitle: String? = null): DomainResult<TeamTaskSolution?> {
        val userId = students.getOrPut(studentName) { UserId(UUID.randomUUID()) }
        val teamId = userTeam[userId] ?: return DomainResult.Success(null)
        val task = findTask(taskTitle ?: currentTaskTitle) as? TeamTaskPost
            ?: return failure(DomainError.NotFound)
        val taskId = TaskId(task.id.value)
        val solution = teamSolutions[taskId to teamId]
        if (solution != null) {
            val updated =
                solution.copy(
                    peerReviewProgress =
                        if (task.gradingMode == GradingMode.PEER_TO_PEER) {
                            buildTeamProgress(task, userId)
                        } else {
                            null
                        },
                )
            teamSolutions[taskId to teamId] = updated
            lastTeamSolution = updated
            return DomainResult.Success(updated)
        }
        lastTeamSolution = null
        return DomainResult.Success(null)
    }

    fun teacherReviewSolution(score: Int = 8): DomainResult<Unit> {
        val solution = individualSolutions.values.firstOrNull()
            ?: return failure(DomainError.NotFound)
        individualSolutions.entries.first { it.value.id == solution.id }.setValue(
            solution.copy(
                status = SolutionStatus.CHECKED,
                score = Score(score),
            ),
        )
        teacherReviewQueue.remove(solution.id)
        lastSolution = individualSolutions.values.first { it.id == solution.id }
        return DomainResult.Success(Unit)
    }

    fun findTask(title: String?): Post? =
        title?.let { t -> posts.values.firstOrNull { it.title == t } }

    fun findCurrentTask(): Post? = findTask(currentTaskTitle)

    fun findTaskById(taskId: TaskId): Post? =
        posts.values.firstOrNull {
            when (it) {
                is TaskPost -> TaskId(it.id.value) == taskId
                is TeamTaskPost -> TaskId(it.id.value) == taskId
                else -> false
            }
        }

    fun completedReviewsCount(userId: UserId, taskId: TaskId): Int =
        peerReviews.values.count {
            it.reviewerId == userId && it.taskId == taskId && it.status == PeerReviewStatus.COMPLETED
        }

    private fun buildIndividualProgress(task: TaskPost, userId: UserId): PeerReviewProgress {
        val required = task.minPeerReviewsRequired ?: 1
        val completed = completedReviewsCount(userId, TaskId(task.id.value))
        val hasSolution = individualSolutions[TaskId(task.id.value) to userId] != null
        val solutionId = individualSolutions[TaskId(task.id.value) to userId]?.id
        val isCounted = solutionId?.let { solutionCounted[it] == true } ?: false
        return PeerReviewProgress(
            required = required,
            completed = completed,
            canFinish = completed >= required,
            isCounted = isCounted,
        )
    }

    private fun buildTeamProgress(task: TeamTaskPost, userId: UserId): PeerReviewProgress {
        val teamId = userTeam[userId]
        val taskId = TaskId(task.id.value)
        val hasTeamSolution = teamId != null && teamSolutions.containsKey(taskId to teamId)
        val completed =
            peerReviews.values.count {
                it.reviewerId == userId && it.taskId == taskId && it.status == PeerReviewStatus.COMPLETED
            }
        val isCounted = hasTeamSolution && completed >= 1
        return PeerReviewProgress(
            required = 1,
            completed = completed,
            canFinish = completed >= 1,
            isCounted = isCounted,
        )
    }

    private fun hasAnySolutions(task: Post): Boolean {
        val taskId = TaskId(when (task) {
            is TaskPost -> task.id.value
            is TeamTaskPost -> task.id.value
            else -> return false
        })
        return individualSolutions.keys.any { it.first == taskId } ||
            teamSolutions.keys.any { it.first == taskId }
    }

    private fun teamNameById(teamId: TeamId): String =
        teams.entries.firstOrNull { it.value == teamId }?.key ?: "Команда"

    private fun teamDto(teamId: TeamId, name: String): Team =
        Team(
            id = teamId,
            name = name,
            members =
                teamMembers[teamId].orEmpty().map {
                    TeamMember(it, "", TeamMemberRole.MEMBER)
                },
        )

    private fun PeerReviewRecord.toTarget(task: TaskPost): PeerReviewTarget {
        val solution = individualSolutions[taskId to checkNotNull(solutionAuthorId)]!!
        return PeerReviewTarget(
            reviewId = id,
            taskId = taskId,
            solution = AnonymizedSolution(solution.text, solution.files),
            criteria = task.gradingRubric?.criteria.orEmpty(),
            assignedAt = assignedAt,
        )
    }

    private fun <T> failure(error: DomainError): DomainResult<T> {
        lastError = error
        return DomainResult.Failure(error)
    }

    enum class PeerReviewStatus { ASSIGNED, COMPLETED }

    class PeerReviewRecord(
        val id: PeerReviewId,
        val taskId: TaskId,
        val reviewerId: UserId,
        val solutionId: SolutionId?,
        val solutionAuthorId: UserId?,
        var status: PeerReviewStatus,
        val assignedAt: OffsetDateTime,
        var completedAt: OffsetDateTime?,
        val reviewerTeamId: TeamId?,
        val teamSolutionId: SolutionId?,
    )
}
