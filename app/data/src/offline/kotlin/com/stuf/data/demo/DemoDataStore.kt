package com.stuf.data.demo

import com.stuf.domain.model.Comment
import com.stuf.domain.model.CommentAuthor
import com.stuf.domain.model.Course
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseMember
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.FileInfo
import com.stuf.domain.model.GradeCell
import com.stuf.domain.model.GradeRow
import com.stuf.domain.model.GradeStatus
import com.stuf.domain.model.GradeTable
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.PostKind
import com.stuf.domain.model.Review
import com.stuf.domain.model.Solution
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.model.TaskDetails
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.UserCourse
import com.stuf.domain.model.UserId
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

        val welcome = Post(
            id = DemoIds.postWelcome,
            courseId = DemoIds.courseAlgebra,
            kind = PostKind.ANNOUNCEMENT,
            title = "Добро пожаловать на курс",
            text = "Здесь будут объявления и материалы. Это офлайн-режим с демо-данными.",
            createdAt = t.minusDays(2),
            taskDetails = null,
        )
        val homework = Post(
            id = DemoIds.postHomework,
            courseId = DemoIds.courseAlgebra,
            kind = PostKind.TASK,
            title = "Домашняя работа №1",
            text = "Решите уравнения 1–5 из учебника. Срок — неделя.",
            createdAt = t.minusDays(1),
            taskDetails = TaskDetails(
                deadline = t.plusDays(7),
                isMandatory = true,
                maxScore = 5,
            ),
        )
        val webLab = Post(
            id = DemoIds.postWebLab,
            courseId = DemoIds.courseWeb,
            kind = PostKind.TASK,
            title = "Лабораторная: форма входа",
            text = "Сверстайте страницу входа на HTML/CSS.",
            createdAt = t,
            taskDetails = TaskDetails(deadline = t.plusDays(14), isMandatory = false, maxScore = 10),
        )
        val clubWelcome = Post(
            id = DemoIds.postClubWelcome,
            courseId = DemoIds.courseTeacherClub,
            kind = PostKind.ANNOUNCEMENT,
            title = "Правила кружка",
            text = "Вы ведёте этот курс в демо-режиме. Ученик «Ученик для демо» — для проверки вкладки участников.",
            createdAt = t.minusHours(1),
            taskDetails = null,
        )

        postsByCourse[DemoIds.courseAlgebra] = mutableListOf(welcome, homework)
        postsByCourse[DemoIds.courseWeb] = mutableListOf(webLab)
        postsByCourse[DemoIds.courseTeacherClub] = mutableListOf(clubWelcome)
        listOf(welcome, homework, webLab, clubWelcome).forEach { postsById[it.id] = it }

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
            comment("c-welcome-1", DemoIds.userStudent, "Спасибо!", t.minusDays(1)),
        )
        commentsBySolution[DemoIds.solutionHomework] = mutableListOf(
            comment("c-sol-1", DemoIds.userTeacher, "Принято, жду проверки", t.minusHours(2)),
        )
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
        text: String,
        at: OffsetDateTime,
    ): Comment = Comment(
        id = id,
        author = CommentAuthor(id = authorId, credentials = "Демо"),
        text = text,
        createdAt = at,
        isPrivate = false,
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

    suspend fun createPost(courseId: CourseId, post: Post): Post = mutex.withLock {
        val newId = PostId(UUID.randomUUID())
        val created = post.copy(id = newId, courseId = courseId)
        postsById[newId] = created
        postsByCourse.getOrPut(courseId) { mutableListOf() }.add(0, created)
        created
    }

    suspend fun updatePost(postId: PostId, post: Post): Post? = mutex.withLock {
        val old = postsById[postId] ?: return@withLock null
        val updated = post.copy(id = postId, courseId = old.courseId)
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
        if (old.kind == PostKind.TASK) {
            solutionsByTask.remove(TaskId(postId.value))
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
        val c = comment(
            id = UUID.randomUUID().toString(),
            authorId = DemoIds.userStudent,
            text = text,
            at = OffsetDateTime.now(),
        )
        commentsByPost.getOrPut(postId) { mutableListOf() }.add(c)
        c
    }

    suspend fun addSolutionComment(solutionId: SolutionId, text: String): Comment = mutex.withLock {
        val c = comment(
            id = UUID.randomUUID().toString(),
            authorId = DemoIds.userStudent,
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
            val c = comment(
                id = UUID.randomUUID().toString(),
                authorId = DemoIds.userStudent,
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
            .filter { it.kind == PostKind.TASK }
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
}
