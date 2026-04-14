package com.stuf.data.demo

import com.stuf.domain.model.Comment
import com.stuf.domain.model.CommentAuthor
import com.stuf.domain.model.Course
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseMember
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.FileInfo
import com.stuf.domain.model.GradeDistribution
import com.stuf.domain.model.GradeDistributionEntry
import com.stuf.domain.model.GradeVote
import com.stuf.domain.model.GradeCell
import com.stuf.domain.model.GradeRow
import com.stuf.domain.model.GradeStatus
import com.stuf.domain.model.GradeTable
import com.stuf.domain.model.AnnouncementPost
import com.stuf.domain.model.MaterialPost
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostAttachment
import com.stuf.domain.model.PostId
import com.stuf.domain.model.TaskPost
import com.stuf.domain.model.TeamTaskPost
import com.stuf.domain.model.isTask
import com.stuf.domain.model.Review
import com.stuf.domain.model.Score
import com.stuf.domain.model.Solution
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.model.TaskDetails
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.Team
import com.stuf.domain.model.TeamCaptainSelectionMode
import com.stuf.domain.model.TeamId
import com.stuf.domain.model.TeamMember
import com.stuf.domain.model.TeamMemberRole
import com.stuf.domain.model.TeamTaskSolution
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.UserCourse
import com.stuf.domain.model.UserId
import com.stuf.domain.model.UserRef
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.text.Charsets
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoDataStore @Inject constructor() {

    private val mutex = Mutex()

    private val courses = mutableMapOf<CourseId, Course>()
    private val inviteToCourse = mutableMapOf<String, CourseId>()
    private val userCourses = mutableListOf<UserCourse>()
    private val members = mutableMapOf<CourseId, MutableList<CourseMember>>()
    private val postsByCourse = mutableMapOf<CourseId, MutableList<Post>>()
    private val postsById = mutableMapOf<PostId, Post>()
    private val solutionsByTask = mutableMapOf<TaskId, Solution>()
    private val commentsByPost = mutableMapOf<PostId, MutableList<Comment>>()
    private val commentsBySolution = mutableMapOf<SolutionId, MutableList<Comment>>()
    private val repliesByComment = mutableMapOf<String, MutableList<Comment>>()
    private var fileSeq = 0

    /** Демо-состояние командных заданий (списки команд, «моя» команда, решения). */
    private val teamsByAssignment = mutableMapOf<PostId, MutableList<Team>>()
    private val myTeamByAssignment = mutableMapOf<PostId, Team?>()
    private val teamTaskSolutionsByTask = mutableMapOf<TaskId, TeamTaskSolution>()

    private data class DemoGradeDistributionBucket(
        var teamRawScore: Double,
        val pointsByUser: MutableMap<UserId, Double>,
        val votes: MutableMap<UserId, GradeVote>,
        var distributionChanged: Boolean,
    )

    private val gradeDistributions = mutableMapOf<String, DemoGradeDistributionBucket>()

    init {
        seed()
    }

    private fun seed() {
        val t = OffsetDateTime.now()
        courses[DemoIds.courseAlgebra] = Course(
            id = DemoIds.courseAlgebra,
            title = "Алгебра — 9 класс",
            inviteCode = "ALG2025",
            authorId = DemoIds.userTeacher,
        )
        courses[DemoIds.courseWeb] = Course(
            id = DemoIds.courseWeb,
            title = "Веб-разработка",
            inviteCode = "WEB101",
            authorId = DemoIds.userTeacher,
        )
        courses[DemoIds.courseTeacherClub] = Course(
            id = DemoIds.courseTeacherClub,
            title = "Кружок программирования",
            inviteCode = "CODE7",
            authorId = DemoIds.userStudent,
        )
        inviteToCourse["ALG2025"] = DemoIds.courseAlgebra
        inviteToCourse["WEB101"] = DemoIds.courseWeb
        inviteToCourse["CODE7"] = DemoIds.courseTeacherClub

        userCourses.clear()
        userCourses.add(UserCourse(DemoIds.courseAlgebra, "Алгебра — 9 класс", CourseRole.STUDENT))
        userCourses.add(UserCourse(DemoIds.courseWeb, "Веб-разработка", CourseRole.STUDENT))
        userCourses.add(UserCourse(DemoIds.courseTeacherClub, "Кружок программирования", CourseRole.TEACHER))

        members.clear()
        members[DemoIds.courseAlgebra] = mutableListOf(
            member(DemoIds.userTeacher, "Учитель Иванова", "teacher@demo.local", CourseRole.TEACHER),
            member(DemoIds.userStudent, "Студент Демо", "student@demo.local", CourseRole.STUDENT),
        )
        members[DemoIds.courseWeb] = mutableListOf(
            member(DemoIds.userTeacher, "Учитель Иванова", "teacher@demo.local", CourseRole.TEACHER),
            member(DemoIds.userStudent, "Студент Демо", "student@demo.local", CourseRole.STUDENT),
        )
        members[DemoIds.courseTeacherClub] = mutableListOf(
            member(DemoIds.userStudent, "Вы (преподаватель)", "student@demo.local", CourseRole.TEACHER),
            member(DemoIds.userTeacher, "Ученик для демо", "teacher@demo.local", CourseRole.STUDENT),
        )

        val welcome =
            AnnouncementPost(
                id = DemoIds.postWelcome,
                courseId = DemoIds.courseAlgebra,
                title = "Добро пожаловать на курс",
                text = "Здесь будут объявления и материалы. Это офлайн-режим с демо-данными.",
                createdAt = t.minusDays(2),
            )
        val homework =
            TaskPost(
                id = DemoIds.postHomework,
                courseId = DemoIds.courseAlgebra,
                title = "Домашняя работа №1",
                text = "Решите уравнения 1–5 из учебника. Срок — неделя.",
                createdAt = t.minusDays(1),
                taskDetails =
                    TaskDetails(
                        deadline = t.plusDays(7),
                        isMandatory = true,
                        maxScore = 5,
                    ),
                assignedScore = Score(4),
            )
        val materialAlgebra =
            MaterialPost(
                id = DemoIds.postMaterialAlgebra,
                courseId = DemoIds.courseAlgebra,
                title = "Шпаргалка: квадратные уравнения",
                text = "Формула дискриминанта и примеры — в приложенном PDF (демо).",
                createdAt = t.minusHours(12),
                files =
                    listOf(
                        PostAttachment(id = UUID.randomUUID(), name = "demo-material.pdf"),
                    ),
            )
        val teamAlgebra =
            TeamTaskPost(
                id = DemoIds.postTeamAlgebra,
                courseId = DemoIds.courseAlgebra,
                title = "Командный проект: мини-исследование",
                text =
                    "Демо: три команды — полная (без мест), с пустыми слотами и пустая. " +
                        "Вы не в команде: проверьте карусель и кнопку «Присоединиться».",
                createdAt = t.minusHours(6),
                taskDetails =
                    TaskDetails(
                        deadline = t.plusDays(10),
                        isMandatory = false,
                        maxScore = 10,
                    ),
                minTeamSize = 2,
                maxTeamSize = 4,
                solvableAfterDeadline = false,
                captainMode = TeamCaptainSelectionMode.VOTING_AND_LOTTERY,
                votingDurationHours = 24,
                predefinedTeamsCount = 3,
                allowJoinTeam = true,
                allowLeaveTeam = true,
                allowStudentTransferCaptain = false,
                assignedScore = Score(8),
            )
        val webLab =
            TaskPost(
                id = DemoIds.postWebLab,
                courseId = DemoIds.courseWeb,
                title = "Лабораторная: форма входа",
                text = "Сверстайте страницу входа на HTML/CSS.",
                createdAt = t,
                taskDetails = TaskDetails(deadline = t.plusDays(14), isMandatory = false, maxScore = 10),
                assignedScore = Score(9),
            )
        val teamWebSprint =
            TeamTaskPost(
                id = DemoIds.postTeamWebSprint,
                courseId = DemoIds.courseWeb,
                title = "Командный спринт: UI-прототип",
                text =
                    "Демо: вы — капитан, оценка команде уже выставлена, срок сдачи прошёл — " +
                        "редактирование текста решения недоступно, но «Распределение оценок» " +
                        "по участникам можно менять в любое время.",
                createdAt = t.minusHours(4),
                taskDetails =
                    TaskDetails(
                        deadline = t.minusDays(1),
                        isMandatory = true,
                        maxScore = 8,
                    ),
                minTeamSize = 2,
                maxTeamSize = 5,
                solvableAfterDeadline = true,
                captainMode = TeamCaptainSelectionMode.TEACHER_FIXED,
                votingDurationHours = null,
                predefinedTeamsCount = 1,
                allowJoinTeam = false,
                allowLeaveTeam = false,
                allowStudentTransferCaptain = false,
                assignedScore = Score(7),
            )
        val teamWebCaptainDraft =
            TeamTaskPost(
                id = DemoIds.postTeamWebCaptainDraft,
                courseId = DemoIds.courseWeb,
                title = "Командное UI: только черновик (решение не отправлено)",
                text =
                    "Демо: вы — капитан единственной команды, решение ещё не отправлено. " +
                        "Заполните текст, прикрепите файлы и нажмите «Отправить решение».",
                createdAt = t.minusHours(2),
                taskDetails =
                    TaskDetails(
                        deadline = t.plusDays(14),
                        isMandatory = false,
                        maxScore = 10,
                    ),
                minTeamSize = 1,
                maxTeamSize = 4,
                solvableAfterDeadline = false,
                captainMode = TeamCaptainSelectionMode.VOTING_AND_LOTTERY,
                votingDurationHours = 48,
                predefinedTeamsCount = 1,
                allowJoinTeam = false,
                allowLeaveTeam = true,
                allowStudentTransferCaptain = true,
            )
        val teamOverdue =
            TeamTaskPost(
                id = DemoIds.postTeamOverdue,
                courseId = DemoIds.courseWeb,
                title = "Просроченный дедлайн (демо)",
                text =
                    "Демо: срок сдачи уже прошёл — отправка решения недоступна. " +
                        "Отдельный сценарий: вы участник команды (капитан — Алексей), есть оценка и черновик распределения баллов.",
                createdAt = t.minusDays(10),
                taskDetails =
                    TaskDetails(
                        deadline = t.minusDays(1),
                        isMandatory = true,
                        maxScore = 5,
                    ),
                minTeamSize = 2,
                maxTeamSize = 5,
                solvableAfterDeadline = false,
                captainMode = TeamCaptainSelectionMode.FIRST_MEMBER,
                votingDurationHours = null,
                predefinedTeamsCount = 1,
                allowJoinTeam = false,
                allowLeaveTeam = false,
                allowStudentTransferCaptain = false,
                assignedScore = Score(5),
            )
        val clubWelcome =
            AnnouncementPost(
                id = DemoIds.postClubWelcome,
                courseId = DemoIds.courseTeacherClub,
                title = "Правила кружка",
                text = "Вы ведёте этот курс в демо-режиме. Ученик «Ученик для демо» — для проверки вкладки участников.",
                createdAt = t.minusHours(1),
            )

        postsByCourse[DemoIds.courseAlgebra] = mutableListOf(welcome, homework, materialAlgebra, teamAlgebra)
        postsByCourse[DemoIds.courseWeb] =
            mutableListOf(webLab, teamWebSprint, teamWebCaptainDraft, teamOverdue)
        postsByCourse[DemoIds.courseTeacherClub] = mutableListOf(clubWelcome)
        listOf(
            welcome,
            homework,
            materialAlgebra,
            teamAlgebra,
            webLab,
            teamWebSprint,
            teamWebCaptainDraft,
            teamOverdue,
            clubWelcome,
        ).forEach {
            postsById[it.id] = it
        }

        seedTeamTaskDemoState(t)

        val taskHomework = TaskId(DemoIds.postHomework.value)
        solutionsByTask[taskHomework] = Solution(
            id = DemoIds.solutionHomework,
            taskId = taskHomework,
            authorId = DemoIds.userStudent,
            text = "Ответ: x = 2, y = -1",
            files = emptyList(),
            score = null,
            status = SolutionStatus.PENDING,
            updatedAt = t.minusHours(3),
        )

        commentsByPost[DemoIds.postWelcome] = mutableListOf(
            comment(
                id = "c-welcome-1",
                authorId = DemoIds.userStudent,
                authorName = "Студент Демо",
                text = "Спасибо!",
                at = t.minusDays(1),
            ),
        )
        commentsByPost[DemoIds.postTeamAlgebra] =
            mutableListOf(
                comment(
                    id = "c-alg-pub",
                    authorId = DemoIds.userTeacher,
                    authorName = "Учитель Иванова",
                    text = "Общий комментарий: шаблон отчёта — в материалах курса.",
                    at = t.minusHours(5),
                    isPrivate = false,
                ),
            )
        commentsByPost[DemoIds.postTeamWebSprint] =
            mutableListOf(
                comment(
                    id = "c-web-pub",
                    authorId = DemoIds.userPeerAlex,
                    authorName = "Алексей К.",
                    text = "Готов подключиться к ревью макетов в пятницу.",
                    at = t.minusHours(2),
                    isPrivate = false,
                ),
            )
        commentsBySolution[DemoIds.solutionTeamWebSprint] =
            mutableListOf(
                comment(
                    id = "c-web-priv",
                    authorId = DemoIds.userStudent,
                    authorName = "Студент Демо",
                    text = "Приватно команде: обновил PDF в решении.",
                    at = t.minusHours(1),
                ),
            )
        commentsBySolution[DemoIds.solutionHomework] =
            mutableListOf(
                comment(
                    id = "c-sol-1",
                    authorId = DemoIds.userTeacher,
                    authorName = "Учитель Иванова",
                    text = "Принято, жду проверки",
                    at = t.minusHours(2),
                ),
            )
    }

    private fun seedTeamTaskDemoState(now: OffsetDateTime) {
        teamsByAssignment.clear()
        myTeamByAssignment.clear()
        teamTaskSolutionsByTask.clear()
        gradeDistributions.clear()

        val teamFull =
            Team(
                id = DemoIds.teamAlgebraFull,
                name = "«Дискриминант» (полный состав, без мест)",
                members =
                    listOf(
                        TeamMember(DemoIds.userPeerAlex, "Алексей К.", TeamMemberRole.LEADER),
                        TeamMember(DemoIds.userPeerMaria, "Мария С.", TeamMemberRole.MEMBER),
                        TeamMember(DemoIds.userPeerOleg, "Олег В.", TeamMemberRole.MEMBER),
                        TeamMember(DemoIds.userPeerAnna, "Анна Л.", TeamMemberRole.MEMBER),
                    ),
            )
        val teamPartial =
            Team(
                id = DemoIds.teamAlgebraPartial,
                name = "«Парабола» (есть места)",
                members =
                    listOf(
                        TeamMember(DemoIds.userTeacher, "Учитель Иванова", TeamMemberRole.LEADER),
                        TeamMember(DemoIds.userPeerAlex, "Алексей К.", TeamMemberRole.MEMBER),
                    ),
            )
        val teamEmpty =
            Team(
                id = DemoIds.teamAlgebraEmpty,
                name = "«Новая команда» (никого нет)",
                members = emptyList(),
            )
        teamsByAssignment[DemoIds.postTeamAlgebra] =
            mutableListOf(teamFull, teamPartial, teamEmpty)
        myTeamByAssignment[DemoIds.postTeamAlgebra] = null

        val webTeam =
            Team(
                id = DemoIds.teamWebUiCrew,
                name = "UI Crew",
                members =
                    listOf(
                        TeamMember(DemoIds.userStudent, "Студент Демо", TeamMemberRole.LEADER),
                        TeamMember(DemoIds.userPeerMaria, "Мария С.", TeamMemberRole.MEMBER),
                    ),
            )
        teamsByAssignment[DemoIds.postTeamWebSprint] = mutableListOf(webTeam)
        myTeamByAssignment[DemoIds.postTeamWebSprint] = webTeam

        val teamCaptainSolo =
            Team(
                id = DemoIds.teamWebCaptainSolo,
                name = "Капитан соло",
                members =
                    listOf(
                        TeamMember(DemoIds.userStudent, "Студент Демо", TeamMemberRole.LEADER),
                        TeamMember(DemoIds.userPeerMaria, "Мария С.", TeamMemberRole.MEMBER),
                    ),
            )
        teamsByAssignment[DemoIds.postTeamWebCaptainDraft] = mutableListOf(teamCaptainSolo)
        myTeamByAssignment[DemoIds.postTeamWebCaptainDraft] = teamCaptainSolo

        val teamOverdueCrew =
            Team(
                id = DemoIds.teamWebOverdue,
                name = "Команда «Дедлайн»",
                members =
                    listOf(
                        TeamMember(DemoIds.userPeerAlex, "Алексей К.", TeamMemberRole.LEADER),
                        TeamMember(DemoIds.userStudent, "Студент Демо", TeamMemberRole.MEMBER),
                        TeamMember(DemoIds.userPeerMaria, "Мария С.", TeamMemberRole.MEMBER),
                    ),
            )
        teamsByAssignment[DemoIds.postTeamOverdue] = mutableListOf(teamOverdueCrew)
        myTeamByAssignment[DemoIds.postTeamOverdue] = teamOverdueCrew

        teamTaskSolutionsByTask[TaskId(DemoIds.postTeamWebSprint.value)] =
            TeamTaskSolution(
                id = DemoIds.solutionTeamWebSprint,
                taskId = TaskId(DemoIds.postTeamWebSprint.value),
                text =
                    "Черновик: главный экран, карточки курсов и нижняя навигация. Фидбек приветствуется.",
                files =
                    listOf(
                        FileInfo(id = "demo-team-file-1", name = "ui-wireframe.pdf"),
                    ),
                score = Score(7),
                status = SolutionStatus.CHECKED,
                updatedAt = now,
                team = webTeam,
                submittedBy = UserRef(DemoIds.userStudent, "Студент Демо"),
            )

        teamTaskSolutionsByTask[TaskId(DemoIds.postTeamOverdue.value)] =
            TeamTaskSolution(
                id = DemoIds.solutionTeamOverdue,
                taskId = TaskId(DemoIds.postTeamOverdue.value),
                text = "Демо: решение команды (капитан — Алексей).",
                files = emptyList(),
                score = Score(5),
                status = SolutionStatus.CHECKED,
                updatedAt = now,
                team = teamOverdueCrew,
                submittedBy = UserRef(DemoIds.userPeerAlex, "Алексей К."),
            )

        val overdueGradeKey = gradeDistributionKey(DemoIds.teamWebOverdue, DemoIds.postTeamOverdue)
        gradeDistributions[overdueGradeKey] =
            DemoGradeDistributionBucket(
                teamRawScore = 5.0,
                pointsByUser =
                    mutableMapOf(
                        DemoIds.userPeerAlex to 2.5,
                        DemoIds.userStudent to 1.5,
                        DemoIds.userPeerMaria to 1.0,
                    ),
                votes = mutableMapOf(),
                distributionChanged = true,
            )
    }

    private fun findTeamSlot(teamId: TeamId): Triple<PostId, Int, Team>? {
        for ((postId, list) in teamsByAssignment) {
            val idx = list.indexOfFirst { it.id == teamId }
            if (idx >= 0) {
                return Triple(postId, idx, list[idx])
            }
        }
        return null
    }

    suspend fun getTeamTaskTeams(assignmentId: PostId): List<Team> =
        mutex.withLock {
            teamsByAssignment[assignmentId].orEmpty().map { it.copy(members = it.members.toList()) }
        }

    suspend fun getTeamTaskMyTeam(assignmentId: PostId): Team? =
        mutex.withLock {
            val team: Team = myTeamByAssignment[assignmentId] ?: return@withLock null
            team.copy(members = team.members.toList())
        }

    suspend fun getTeamTaskSolution(taskId: TaskId): TeamTaskSolution? =
        mutex.withLock {
            teamTaskSolutionsByTask[taskId]
        }

    suspend fun demoJoinTeam(teamId: TeamId): DomainResult<Unit> =
        mutex.withLock {
            val slot = findTeamSlot(teamId)
                ?: return@withLock DomainResult.Failure(
                    DomainError.Validation("Команда не найдена"),
                )
            val (postId, index, team) = slot
            if (myTeamByAssignment[postId] != null) {
                return@withLock DomainResult.Failure(
                    DomainError.Validation("Вы уже в команде по этому заданию"),
                )
            }
            val post = postsById[postId] as? TeamTaskPost
                ?: return@withLock DomainResult.Failure(
                    DomainError.Unknown(),
                )
            val maxTeamSize: Int = post.maxTeamSize ?: Int.MAX_VALUE
            val canJoinByPolicy = post.allowJoinTeam ?: true
            if (!canJoinByPolicy) {
                return@withLock DomainResult.Failure(
                    DomainError.Validation("Вступление в команды отключено"),
                )
            }
            if (team.members.size >= maxTeamSize) {
                return@withLock DomainResult.Failure(
                    DomainError.Validation("В команде нет свободных мест"),
                )
            }
            if (team.members.any { it.userId == DemoIds.userStudent }) {
                return@withLock DomainResult.Success(Unit)
            }
            val updated: Team =
                team.copy(
                    members =
                        team.members +
                            TeamMember(
                                DemoIds.userStudent,
                                "Студент Демо",
                                TeamMemberRole.MEMBER,
                            ),
                )
            teamsByAssignment[postId]!![index] = updated
            myTeamByAssignment[postId] = updated
            DomainResult.Success(Unit)
        }

    suspend fun demoLeaveTeam(teamId: TeamId): DomainResult<Unit> =
        mutex.withLock {
            val slot = findTeamSlot(teamId)
                ?: return@withLock DomainResult.Failure(
                    DomainError.Validation("Команда не найдена"),
                )
            val (postId, index, team) = slot
            postsById[postId] as? TeamTaskPost
                ?: return@withLock DomainResult.Failure(DomainError.Unknown())
            val post = postsById[postId] as TeamTaskPost
            if (post.allowLeaveTeam == false) {
                return@withLock DomainResult.Failure(
                    DomainError.Validation("Выход из команды отключен"),
                )
            }
            if (!team.members.any { it.userId == DemoIds.userStudent }) {
                return@withLock DomainResult.Failure(
                    DomainError.Validation("Вы не состоите в этой команде"),
                )
            }
            val newMembers: List<TeamMember> =
                team.members.filter { it.userId != DemoIds.userStudent }
            val updated: Team = team.copy(members = newMembers)
            teamsByAssignment[postId]!![index] = updated
            myTeamByAssignment[postId] = null
            teamTaskSolutionsByTask.remove(TaskId(postId.value))
            DomainResult.Success(Unit)
        }

    suspend fun demoIsCaptain(teamId: TeamId): Boolean =
        mutex.withLock {
            val slot = findTeamSlot(teamId) ?: return@withLock false
            slot.third.members.any {
                it.userId == DemoIds.userStudent && it.role == TeamMemberRole.LEADER
            }
        }

    suspend fun demoTransferCaptain(teamId: TeamId, toUserId: UserId): DomainResult<Unit> =
        mutex.withLock {
            val slot = findTeamSlot(teamId)
                ?: return@withLock DomainResult.Failure(DomainError.Validation("Команда не найдена"))
            val (postId, index, team) = slot
            val post = postsById[postId] as? TeamTaskPost
                ?: return@withLock DomainResult.Failure(DomainError.Unknown())
            if (post.allowStudentTransferCaptain != true) {
                return@withLock DomainResult.Failure(
                    DomainError.Validation("Передача капитана отключена"),
                )
            }
            val isCurrentUserLeader =
                team.members.any {
                    it.userId == DemoIds.userStudent && it.role == TeamMemberRole.LEADER
                }
            if (!isCurrentUserLeader) {
                return@withLock DomainResult.Failure(
                    DomainError.Validation("Только капитан может передавать роль"),
                )
            }
            if (team.members.none { it.userId == toUserId }) {
                return@withLock DomainResult.Failure(
                    DomainError.Validation("Кандидат не состоит в команде"),
                )
            }
            if (toUserId == DemoIds.userStudent) {
                return@withLock DomainResult.Success(Unit)
            }
            val updatedMembers =
                team.members.map { member ->
                    when (member.userId) {
                        toUserId -> member.copy(role = TeamMemberRole.LEADER)
                        DemoIds.userStudent -> member.copy(role = TeamMemberRole.MEMBER)
                        else -> member
                    }
                }
            val updatedTeam = team.copy(members = updatedMembers)
            teamsByAssignment[postId]!![index] = updatedTeam
            myTeamByAssignment[postId] = updatedTeam
            DomainResult.Success(Unit)
        }

    suspend fun demoVoteCaptain(teamId: TeamId, candidateId: UserId): DomainResult<Unit> =
        mutex.withLock {
            val slot = findTeamSlot(teamId)
                ?: return@withLock DomainResult.Failure(DomainError.Validation("Команда не найдена"))
            val (postId, _, team) = slot
            val post = postsById[postId] as? TeamTaskPost
                ?: return@withLock DomainResult.Failure(DomainError.Unknown())
            if (post.captainMode != TeamCaptainSelectionMode.VOTING_AND_LOTTERY) {
                return@withLock DomainResult.Failure(
                    DomainError.Validation("Голосование за капитана недоступно"),
                )
            }
            if (team.members.none { it.userId == DemoIds.userStudent }) {
                return@withLock DomainResult.Failure(
                    DomainError.Validation("Вы не состоите в этой команде"),
                )
            }
            if (team.members.none { it.userId == candidateId }) {
                return@withLock DomainResult.Failure(
                    DomainError.Validation("Кандидат не состоит в команде"),
                )
            }
            DomainResult.Success(Unit)
        }

    suspend fun demoSubmitTeamSolution(
        taskId: TaskId,
        text: String?,
        fileIds: List<String>,
    ): DomainResult<SolutionId> =
        mutex.withLock {
            val postId = PostId(taskId.value)
            val teamPost = postsById[postId] as? TeamTaskPost
                ?: return@withLock DomainResult.Failure(DomainError.Unknown())
            val deadline = teamPost.taskDetails.deadline
            if (deadline != null && deadline.isBefore(OffsetDateTime.now())) {
                return@withLock DomainResult.Failure(
                    DomainError.Validation("Срок сдачи прошёл"),
                )
            }
            val myTeam = myTeamByAssignment[postId] ?: return@withLock DomainResult.Failure(
                DomainError.Validation("Нет команды"),
            )
            if (!myTeam.members.any { it.userId == DemoIds.userStudent && it.role == TeamMemberRole.LEADER }) {
                return@withLock DomainResult.Failure(
                    DomainError.Validation("Только капитан"),
                )
            }
            val files =
                fileIds.mapIndexed { i, id ->
                    FileInfo(id = id, name = "upload-$i")
                }
            val newId = SolutionId(UUID.randomUUID())
            val solution =
                TeamTaskSolution(
                    id = newId,
                    taskId = taskId,
                    text = text,
                    files = files,
                    score = null,
                    status = SolutionStatus.PENDING,
                    updatedAt = OffsetDateTime.now(),
                    team = myTeam,
                    submittedBy = UserRef(DemoIds.userStudent, "Студент Демо"),
                )
            teamTaskSolutionsByTask[taskId] = solution
            DomainResult.Success(newId)
        }

    private fun member(
        id: UserId,
        name: String,
        email: String,
        role: CourseRole,
    ): CourseMember = CourseMember(
        id = id,
        credentials = name,
        email = email,
        role = role,
    )

    private fun comment(
        id: String,
        authorId: UserId,
        authorName: String,
        text: String,
        at: OffsetDateTime,
        isPrivate: Boolean = false,
    ): Comment =
        Comment(
            id = id,
            author = CommentAuthor(id = authorId, credentials = authorName),
            text = text,
            createdAt = at,
            isPrivate = isPrivate,
        )

    suspend fun getUserCourses(): List<UserCourse> = mutex.withLock { userCourses.toList() }

    suspend fun createCourse(title: String, authorId: UserId): Course = mutex.withLock {
        val id = CourseId(UUID.randomUUID())
        val code = "DEMO-${id.value.toString().take(8).uppercase()}"
        val course = Course(id = id, title = title, inviteCode = code, authorId = authorId)
        courses[id] = course
        inviteToCourse[code] = id
        userCourses.add(UserCourse(id, title, CourseRole.TEACHER))
        members[id] = mutableListOf(
            member(authorId, "Вы (учитель)", "teacher@demo.local", CourseRole.TEACHER),
        )
        postsByCourse[id] = mutableListOf()
        course
    }

    suspend fun joinCourse(inviteCode: String): Course? = mutex.withLock {
        val id = inviteToCourse[inviteCode.trim()] ?: return@withLock null
        val course = courses[id] ?: return@withLock null
        if (userCourses.none { it.id == id }) {
            userCourses.add(UserCourse(id, course.title, CourseRole.STUDENT))
        }
        val list = members.getOrPut(id) { mutableListOf() }
        if (list.none { it.id == DemoIds.userStudent }) {
            list.add(member(DemoIds.userStudent, "Студент Демо", "student@demo.local", CourseRole.STUDENT))
        }
        course
    }

    suspend fun getCourseInfo(courseId: CourseId): Course? = mutex.withLock { courses[courseId] }

    suspend fun getCourseMembers(courseId: CourseId, query: String?): List<CourseMember> =
        mutex.withLock {
            val list = members[courseId].orEmpty()
            val q = query?.trim()?.lowercase().orEmpty()
            if (q.isEmpty()) list
            else list.filter {
                it.credentials.lowercase().contains(q) || it.email.lowercase().contains(q)
            }
        }

    suspend fun changeMemberRole(
        courseId: CourseId,
        userId: UserId,
        newRole: CourseRole,
    ): Boolean = mutex.withLock {
        val list = members[courseId] ?: return@withLock false
        val i = list.indexOfFirst { it.id == userId }
        if (i < 0) return@withLock false
        val m = list[i]
        list[i] = m.copy(role = newRole)
        true
    }

    suspend fun removeMember(courseId: CourseId, userId: UserId): Boolean = mutex.withLock {
        val list = members[courseId] ?: return@withLock false
        return@withLock list.removeAll { it.id == userId }
    }

    suspend fun leaveCourse(courseId: CourseId): Boolean = mutex.withLock {
        userCourses.removeAll { it.id == courseId }
        members[courseId]?.removeAll { it.id == DemoIds.userStudent }
        true
    }

    suspend fun getCourseFeed(courseId: CourseId, skip: Int, take: Int): List<Post> =
        mutex.withLock {
            val all = postsByCourse[courseId].orEmpty()
            all.drop(skip).take(take)
        }

    suspend fun getPost(postId: PostId): Post? = mutex.withLock { postsById[postId] }

    suspend fun createPost(courseId: CourseId, post: Post): Post =
        mutex.withLock {
            val newId = PostId(UUID.randomUUID())
            val created =
                when (post) {
                    is AnnouncementPost -> post.copy(id = newId, courseId = courseId)
                    is MaterialPost -> post.copy(id = newId, courseId = courseId)
                    is TaskPost -> post.copy(id = newId, courseId = courseId)
                    is TeamTaskPost -> post.copy(id = newId, courseId = courseId)
                }
            postsById[newId] = created
            postsByCourse.getOrPut(courseId) { mutableListOf() }.add(0, created)
            created
        }

    suspend fun updatePost(postId: PostId, post: Post): Post? =
        mutex.withLock {
            val old = postsById[postId] ?: return@withLock null
            val updated =
                when (post) {
                    is AnnouncementPost -> post.copy(id = postId, courseId = old.courseId)
                    is MaterialPost -> post.copy(id = postId, courseId = old.courseId)
                    is TaskPost -> post.copy(id = postId, courseId = old.courseId)
                    is TeamTaskPost -> post.copy(id = postId, courseId = old.courseId)
                }
            postsById[postId] = updated
            val list = postsByCourse[old.courseId]
            val idx = list?.indexOfFirst { it.id == postId } ?: -1
            if (idx >= 0) list!![idx] = updated
            updated
        }

    suspend fun deletePost(postId: PostId): Boolean = mutex.withLock {
        val old = postsById.remove(postId) ?: return@withLock false
        postsByCourse[old.courseId]?.removeAll { it.id == postId }
        commentsByPost.remove(postId)
        if (old.isTask()) {
            solutionsByTask.remove(TaskId(postId.value))
        }
        if (old is TeamTaskPost) {
            teamsByAssignment.remove(postId)
            myTeamByAssignment.remove(postId)
            teamTaskSolutionsByTask.remove(TaskId(postId.value))
        }
        true
    }

    suspend fun submitSolution(
        taskId: TaskId,
        text: String?,
        fileIds: List<String>,
    ): Solution = mutex.withLock {
        val id = SolutionId(UUID.randomUUID())
        val sol = Solution(
            id = id,
            taskId = taskId,
            authorId = DemoIds.userStudent,
            text = text,
            files = fileIds.mapIndexed { i, fid -> FileInfo(id = fid, name = "file$i") },
            score = null,
            status = SolutionStatus.PENDING,
            updatedAt = OffsetDateTime.now(),
        )
        solutionsByTask[taskId] = sol
        sol
    }

    suspend fun cancelSolution(taskId: TaskId): Boolean = mutex.withLock {
        solutionsByTask.remove(taskId) != null
    }

    suspend fun getUserSolution(taskId: TaskId): Solution? = mutex.withLock {
        solutionsByTask[taskId]?.takeIf { it.authorId == DemoIds.userStudent }
    }

    suspend fun getTaskSolutions(
        taskId: TaskId,
        status: SolutionStatus?,
        studentId: UserId?,
    ): List<Solution> = mutex.withLock {
        solutionsByTask[taskId]?.let { listOf(it) }.orEmpty()
            .filter { status == null || it.status == status }
            .filter { studentId == null || it.authorId == studentId }
    }

    suspend fun reviewSolution(solutionId: SolutionId, review: Review): Boolean = mutex.withLock {
        val entry = solutionsByTask.entries.find { it.value.id == solutionId } ?: return@withLock false
        val old = entry.value
        val updated = old.copy(
            score = review.score,
            status = review.status,
            updatedAt = OffsetDateTime.now(),
        )
        solutionsByTask[entry.key] = updated
        true
    }

    suspend fun getPostComments(postId: PostId): List<Comment> =
        mutex.withLock { commentsByPost[postId].orEmpty().toList() }

    suspend fun getSolutionComments(solutionId: SolutionId): List<Comment> =
        mutex.withLock { commentsBySolution[solutionId].orEmpty().toList() }

    suspend fun addPostComment(postId: PostId, text: String): Comment = mutex.withLock {
        val c =
            comment(
                id = UUID.randomUUID().toString(),
                authorId = DemoIds.userStudent,
                authorName = "Студент Демо",
                text = text,
                at = OffsetDateTime.now(),
            )
        commentsByPost.getOrPut(postId) { mutableListOf() }.add(c)
        c
    }

    suspend fun addSolutionComment(solutionId: SolutionId, text: String): Comment = mutex.withLock {
        val c =
            comment(
                id = UUID.randomUUID().toString(),
                authorId = DemoIds.userStudent,
                authorName = "Студент Демо",
                text = text,
                at = OffsetDateTime.now(),
            )
        commentsBySolution.getOrPut(solutionId) { mutableListOf() }.add(c)
        c
    }

    suspend fun getCommentReplies(commentId: com.stuf.domain.model.CommentId): List<Comment> =
        mutex.withLock { repliesByComment[commentId.value].orEmpty().toList() }

    suspend fun addCommentReply(commentId: com.stuf.domain.model.CommentId, text: String): Comment =
        mutex.withLock {
            val c =
                comment(
                    id = UUID.randomUUID().toString(),
                    authorId = DemoIds.userStudent,
                    authorName = "Студент Демо",
                    text = text,
                    at = OffsetDateTime.now(),
                )
            repliesByComment.getOrPut(commentId.value) { mutableListOf() }.add(c)
            c
        }

    suspend fun uploadFile(bytes: ByteArray, name: String): FileInfo = mutex.withLock {
        fileSeq++
        FileInfo(id = "demo-file-$fileSeq-${bytes.size}", name = name)
    }

    suspend fun getPerformanceTable(courseId: CourseId): GradeTable = mutex.withLock {
        val taskPosts = postsByCourse[courseId].orEmpty()
            .filter { it.isTask() }
            .map { TaskId(it.id.value) }
        val students = members[courseId].orEmpty().filter { it.role == CourseRole.STUDENT }
        val rows = students.map { m ->
            val cells = taskPosts.associateWith { tid ->
                val sol = solutionsByTask[tid]
                when {
                    sol == null || sol.authorId != m.id -> GradeCell(GradeStatus.NOT_SUBMITTED, null)
                    sol.status == SolutionStatus.PENDING -> GradeCell(GradeStatus.PENDING_REVIEW, null)
                    sol.status == SolutionStatus.RETURNED -> GradeCell(GradeStatus.RETURNED, sol.score)
                    sol.status == SolutionStatus.CHECKED -> GradeCell(GradeStatus.GRADED, sol.score)
                    else -> GradeCell(GradeStatus.PENDING_REVIEW, sol.score)
                }
            }
            val scores = cells.values.mapNotNull { it.score?.value?.toDouble() }
            val avg = if (scores.isEmpty()) 0.0 else scores.average()
            GradeRow(
                student = CourseMember(
                    id = m.id,
                    credentials = m.credentials,
                    email = m.email,
                    role = m.role,
                ),
                cells = cells,
                averageScore = avg,
            )
        }
        GradeTable(tasks = taskPosts, rows = rows)
    }

    private fun gradeDistributionKey(teamId: TeamId, assignmentId: PostId): String =
        "${teamId.value}|${assignmentId.value}"

    private fun ensureGradeDistributionBucketLocked(
        teamId: TeamId,
        assignmentId: PostId,
    ): DemoGradeDistributionBucket? {
        val team = teamsByAssignment[assignmentId]?.find { it.id == teamId } ?: return null
        val taskId = TaskId(assignmentId.value)
        val solution = teamTaskSolutionsByTask[taskId] ?: return null
        val sc = solution.score ?: return null
        val rraw = sc.value.toDouble()
        val key = gradeDistributionKey(teamId, assignmentId)
        return gradeDistributions.getOrPut(key) {
            DemoGradeDistributionBucket(
                teamRawScore = rraw,
                pointsByUser = team.members.associateTo(mutableMapOf()) { it.userId to 0.0 },
                votes = mutableMapOf(),
                distributionChanged = false,
            )
        }.also { bucket ->
            bucket.teamRawScore = rraw
            for (m in team.members) {
                bucket.pointsByUser.putIfAbsent(m.userId, 0.0)
            }
        }
    }

    suspend fun gradeDistributionGet(
        teamId: TeamId,
        assignmentId: PostId,
        currentUserId: UserId?,
    ): DomainResult<GradeDistribution> =
        mutex.withLock {
            val team = teamsByAssignment[assignmentId]?.find { it.id == teamId }
                ?: return@withLock DomainResult.Failure(DomainError.Validation("Команда не найдена"))
            val bucket = ensureGradeDistributionBucketLocked(teamId, assignmentId)
                ?: return@withLock DomainResult.Failure(DomainError.Validation("Оценка ещё не выставлена"))
            val entries =
                team.members.map { m ->
                    GradeDistributionEntry(m.userId, bucket.pointsByUser[m.userId] ?: 0.0)
                }
            val sum = entries.sumOf { it.points }
            DomainResult.Success(
                GradeDistribution(
                    teamId = teamId,
                    assignmentId = assignmentId,
                    teamRawScore = bucket.teamRawScore,
                    entries = entries,
                    sumDistributed = sum,
                    distributionChanged = bucket.distributionChanged,
                    currentUserVote = currentUserId?.let { bucket.votes[it] },
                ),
            )
        }

    suspend fun gradeDistributionUpdate(
        teamId: TeamId,
        assignmentId: PostId,
        entries: List<GradeDistributionEntry>,
    ): DomainResult<GradeDistribution> =
        mutex.withLock {
            val team = teamsByAssignment[assignmentId]?.find { it.id == teamId }
                ?: return@withLock DomainResult.Failure(DomainError.Validation("Команда не найдена"))
            if (!team.members.any { it.userId == DemoIds.userStudent && it.role == TeamMemberRole.LEADER }) {
                return@withLock DomainResult.Failure(
                    DomainError.Validation("Только капитан команды может сохранить распределение"),
                )
            }
            val bucket = ensureGradeDistributionBucketLocked(teamId, assignmentId)
                ?: return@withLock DomainResult.Failure(DomainError.Validation("Оценка ещё не выставлена"))
            val memberIds = team.members.map { it.userId }.toSet()
            for (e in entries) {
                if (e.userId !in memberIds) {
                    return@withLock DomainResult.Failure(DomainError.Validation("Неверный участник"))
                }
                bucket.pointsByUser[e.userId] = e.points
            }
            bucket.distributionChanged = true
            val outEntries =
                team.members.map { m ->
                    GradeDistributionEntry(m.userId, bucket.pointsByUser[m.userId] ?: 0.0)
                }
            val sum = outEntries.sumOf { it.points }
            DomainResult.Success(
                GradeDistribution(
                    teamId = teamId,
                    assignmentId = assignmentId,
                    teamRawScore = bucket.teamRawScore,
                    entries = outEntries,
                    sumDistributed = sum,
                    distributionChanged = bucket.distributionChanged,
                    currentUserVote = null,
                ),
            )
        }

    suspend fun gradeDistributionVote(
        teamId: TeamId,
        assignmentId: PostId,
        vote: GradeVote,
        voterId: UserId,
    ): DomainResult<Unit> =
        mutex.withLock {
            val bucket = ensureGradeDistributionBucketLocked(teamId, assignmentId)
                ?: return@withLock DomainResult.Failure(DomainError.Validation("Оценка ещё не выставлена"))
            val team = teamsByAssignment[assignmentId]?.find { it.id == teamId }
                ?: return@withLock DomainResult.Failure(DomainError.Validation("Команда не найдена"))
            if (team.members.none { it.userId == voterId }) {
                return@withLock DomainResult.Failure(DomainError.Validation("Не участник команды"))
            }
            bucket.votes[voterId] = vote
            DomainResult.Success(Unit)
        }
}
