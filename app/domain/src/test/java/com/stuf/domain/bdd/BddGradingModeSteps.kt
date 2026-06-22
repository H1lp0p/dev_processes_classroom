package com.stuf.domain.bdd

import com.stuf.domain.bdd.support.BddWorld
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.GradingMode
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.TeamTaskPost
import com.stuf.domain.model.TaskPost
import io.cucumber.java.ru.Дано
import io.cucumber.java.ru.И
import io.cucumber.java.ru.Когда
import io.cucumber.java.ru.Тогда
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

class BddCommonSteps {

    @Дано("преподаватель авторизован в курсе {string}")
    fun teacherInCourse(course: String) {
        BddWorld.backend.loginTeacher(course)
    }

    @Тогда("система возвращает ошибку BadRequest")
    fun thenBadRequest() {
        assertEquals("BadRequest", BddWorld.errorKind())
    }

    @Тогда("система возвращает ошибку Forbidden")
    fun thenForbidden() {
        assertEquals("Forbidden", BddWorld.errorKind())
    }

    @Тогда("система возвращает ошибку EntryExists")
    fun thenEntryExists() {
        assertTrue(BddWorld.isEntryExists())
    }

    @И("сообщение указывает, что MinPeerReviewsRequired обязателен и должен быть >= 1")
    fun messageMinRequired() {
        val msg = BddWorld.validationMessage().orEmpty()
        assertTrue(msg.contains("MinPeerReviewsRequired", ignoreCase = true))
    }

    @И("сообщение предлагает сначала прикрепить своё решение")
    fun messageAttachSolution() {
        val msg = BddWorld.validationMessage().orEmpty()
        assertTrue(msg.contains("прикрепите", ignoreCase = true))
    }

    @Когда("{string} запрашивает прогресс оценивания")
    fun requestProgress(name: String) = runBlocking {
        BddWorld.backend.loginStudent(name)
        val task =
            BddWorld.backend.findCurrentTask()
                ?: error("Нет текущего задания")
        val result =
            when (task) {
                is TeamTaskPost ->
                    BddWorld.harness.getTeamPeerReviewProgress(TaskId(task.id.value))
                is TaskPost ->
                    BddWorld.harness.getIndividualPeerReviewProgress(TaskId(task.id.value))
                else -> error("Не задание")
            }
        BddWorld.capture(result)
    }
}

class BddGradingModeSteps {

    @Когда("преподаватель создаёт индивидуальное задание")
    fun createIndividual() {
        BddWorld.backend.draftIsTeam = false
        BddWorld.backend.draftTitle = "Индивидуальное задание"
        BddWorld.backend.explicitDefaultGradingMode = false
    }

    @Когда("преподаватель создаёт командное задание")
    fun createTeam() {
        BddWorld.backend.draftIsTeam = true
        BddWorld.backend.draftTitle = "Командное задание"
        BddWorld.backend.explicitDefaultGradingMode = false
    }

    @Когда("преподаватель создаёт задание, не указывая GradingMode")
    fun createDefault() {
        BddWorld.backend.draftIsTeam = false
        BddWorld.backend.draftTitle = "Задание"
        BddWorld.backend.draftGradingMode = null
        BddWorld.backend.explicitDefaultGradingMode = true
        runBlocking {
            val result = BddWorld.harness.createPost(BddWorld.backend.courseId, stubPost())
            BddWorld.capture(result)
            if (result is DomainResult.Success) BddWorld.lastPost = result.value
        }
    }

    @И("указывает GradingMode = {string}")
    fun setGradingMode(mode: String) {
        BddWorld.backend.draftGradingMode = parseMode(mode)
        if (BddWorld.backend.draftIsTeam) {
            runCreate()
        }
    }

    @И("указывает MinPeerReviewsRequired = {int}")
    fun setMinRequired(value: Int) {
        BddWorld.backend.draftMinPeerReviews = value
        runCreate()
    }

    @И("не указывает MinPeerReviewsRequired")
    fun noMinRequired() {
        BddWorld.backend.draftMinPeerReviews = null
        runCreate()
    }

    private fun runCreate() = runBlocking {
        val result = BddWorld.harness.createPost(BddWorld.backend.courseId, stubPost())
        BddWorld.capture(result)
        if (result is DomainResult.Success) BddWorld.lastPost = result.value
    }

    @Тогда("задание успешно создаётся")
    fun assignmentCreated() {
        assertTrue(BddWorld.lastResult is DomainResult.Success)
    }

    @И("у задания GradingMode = {string}")
    fun assertGradingMode(mode: String) {
        val post = BddWorld.lastPost ?: BddWorld.backend.findCurrentTask()
        val actual =
            when (post) {
                is TaskPost -> post.gradingMode
                is TeamTaskPost -> post.gradingMode
                else -> error("Not a task post")
            }
        assertEquals(parseMode(mode), actual)
    }

    @И("у задания MinPeerReviewsRequired = {int}")
    fun assertMinRequired(value: Int) {
        val post = BddWorld.lastPost as TaskPost
        assertEquals(value, post.minPeerReviewsRequired)
    }

    @И("поле MinPeerReviewsRequired не применяется к командному заданию")
    fun teamHasNoMin() {
        assertTrue(BddWorld.lastPost is TeamTaskPost)
    }

    @Дано("существует P2P-задание, по которому уже есть прикреплённые решения")
    fun p2pWithSolutions() {
        val task =
            BddWorld.backend.registerIndividualTask(
                "P2P с решениями",
                GradingMode.PEER_TO_PEER,
                2,
            )
        BddWorld.lastPost = task
        BddWorld.backend.loginStudent("Студент1")
        BddWorld.backend.submitIndividualSolution("Студент1", task.title)
    }

    @Когда("преподаватель пытается изменить GradingMode на {string}")
    fun changeMode(mode: String) = runBlocking {
        val post = BddWorld.backend.findCurrentTask() ?: BddWorld.lastPost!!
        val updated =
            when (post) {
                is TaskPost -> post.copy(gradingMode = parseMode(mode))
                is TeamTaskPost -> post.copy(gradingMode = parseMode(mode))
                else -> error("Not a task")
            }
        val result = BddWorld.harness.updatePost(post.id, updated)
        BddWorld.capture(result)
    }

    @И("режим оценивания остаётся прежним")
    fun modeUnchanged() {
        val post = BddWorld.backend.findCurrentTask()
        assertEquals(GradingMode.PEER_TO_PEER, (post as TaskPost).gradingMode)
    }

    private fun stubPost(): TaskPost =
        TaskPost(
            id = com.stuf.domain.model.PostId(java.util.UUID.randomUUID()),
            courseId = BddWorld.backend.courseId,
            title = BddWorld.backend.draftTitle,
            text = BddWorld.backend.draftTitle,
            createdAt = java.time.OffsetDateTime.now(),
            taskDetails =
                com.stuf.domain.model.TaskDetails(
                    deadline = null,
                    isMandatory = true,
                    maxScore = 10,
                ),
            gradingMode = BddWorld.backend.draftGradingMode ?: GradingMode.TEACHER_REVIEW,
            minPeerReviewsRequired = BddWorld.backend.draftMinPeerReviews,
        )

    private fun parseMode(mode: String): GradingMode =
        when (mode) {
            "PeerToPeer" -> GradingMode.PEER_TO_PEER
            else -> GradingMode.TEACHER_REVIEW
        }
}
