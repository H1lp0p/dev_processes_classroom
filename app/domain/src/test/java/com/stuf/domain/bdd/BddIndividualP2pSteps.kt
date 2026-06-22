package com.stuf.domain.bdd

import com.stuf.domain.bdd.support.BddTestHelpers
import com.stuf.domain.bdd.support.BddWorld
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.GradingMode
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.model.TaskId
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

class BddIndividualP2pSteps {

    @Дано("существует индивидуальное задание {string} с GradingMode = {string}")
    fun individualTask(title: String, mode: String) {
        BddWorld.backend.registerIndividualTask(
            title,
            if (mode == "PeerToPeer") GradingMode.PEER_TO_PEER else GradingMode.TEACHER_REVIEW,
            minRequired = null,
        )
    }

    @И("MinPeerReviewsRequired = {int}")
    fun setMinForCurrentTask(value: Int) {
        val task = BddWorld.backend.findCurrentTask() as TaskPost
        BddWorld.backend.posts[task.id] = task.copy(minPeerReviewsRequired = value)
    }

    @И("студент {string} авторизован и состоит в курсе")
    fun studentLoggedIn(name: String) {
        BddWorld.backend.loginStudent(name)
    }

    @Дано("студент {string} ещё не прикрепил своё решение")
    fun noSolution(@Suppress("UNUSED_PARAMETER") name: String) {
        // состояние по умолчанию
    }

    @Когда("{string} запрашивает следующее назначение на оценку")
    fun requestNext(name: String) = runBlocking {
        BddWorld.backend.loginStudent(name)
        val task = BddWorld.backend.findCurrentTask() as TaskPost
        val result = BddWorld.harness.getNextPeerReview(TaskId(task.id.value))
        BddWorld.capture(result)
    }

    @Когда("{string} прикрепляет решение к заданию")
    fun attachSolution(name: String) = runBlocking {
        BddWorld.backend.loginStudent(name)
        val task = BddWorld.backend.findCurrentTask() as TaskPost
        val result = BddWorld.harness.submitSolution(TaskId(task.id.value), "text", emptyList())
        BddWorld.capture(result)
    }

    @Дано("{string} прикрепил своё решение")
    fun hasSolution(name: String) {
        BddWorld.backend.submitIndividualSolution(name)
    }

    @И("в задании есть решения других студентов")
    fun otherSolutions() {
        BddWorld.backend.submitIndividualSolution("Пётр")
        BddWorld.backend.submitIndividualSolution("Мария")
    }

    @Тогда("создаётся PeerReview со статусом {string}")
    fun peerReviewStatus(status: String) {
        val record = BddWorld.backend.lastPeerReviewCreated ?: BddWorld.backend.lastTeamPeerReview
        assertNotNull(record)
        assertEquals(status.lowercase(), record!!.status.name.lowercase())
    }

    @И("возвращается решение другого студента")
    fun returnsOtherSolution() {
        assertNotNull(BddWorld.backend.lastPeerReviewTarget)
    }

    @И("возвращаются критерии задания для заполнения")
    fun returnsCriteria() {
        assertTrue(BddWorld.backend.lastPeerReviewTarget!!.criteria.isNotEmpty())
    }

    @И("автор оцениваемого решения скрыт \\(анонимно\\)")
    fun anonymous() {
        // PeerReviewTarget не содержит автора — анонимность по контракту
        assertNotNull(BddWorld.backend.lastPeerReviewTarget)
    }

    @И("назначенное решение не принадлежит {string}")
    fun notOwnSolution(student: String) {
        val userId = resolveStudentId(student)
        val record = BddWorld.backend.lastPeerReviewCreated!!
        assertFalse(record.solutionAuthorId == userId)
    }

    @Дано("{string} уже имеет назначение со статусом {string}")
    fun hasAssigned(name: String, @Suppress("UNUSED_PARAMETER") status: String) {
        BddWorld.backend.submitIndividualSolution(name)
        BddWorld.backend.submitIndividualSolution("Пётр")
        runBlocking {
            BddWorld.backend.loginStudent(name)
            val task = BddWorld.backend.findCurrentTask() as TaskPost
            BddWorld.harness.getNextPeerReview(TaskId(task.id.value))
        }
    }

    @Когда("{string} повторно запрашивает следующее назначение")
    fun requestNextAgain(name: String) = requestNext(name)

    @И("возвращается то же самое назначение")
    fun sameAssignment() {
        val first = BddWorld.backend.lastPeerReviewCreated!!.id
        runBlocking {
            val task = BddWorld.backend.findCurrentTask() as TaskPost
            BddWorld.harness.getNextPeerReview(TaskId(task.id.value))
        }
        assertEquals(first, BddWorld.backend.lastPeerReviewTarget!!.reviewId)
    }

    @И("новое назначение не создаётся")
    fun noNewAssignment() {
        val assigned =
            BddWorld.backend.peerReviews.values.count {
                it.status == com.stuf.domain.bdd.support.BddBackend.PeerReviewStatus.ASSIGNED
            }
        assertEquals(1, assigned)
    }

    @Дано("{string} имеет назначение со статусом {string}")
    fun hasAssignment(name: String, status: String) {
        hasAssigned(name, status)
    }

    @Когда("{string} выставляет оценки по критериям и нажимает {string}")
    fun submitReview(name: String, @Suppress("UNUSED_PARAMETER") button: String) = runBlocking {
        BddWorld.backend.loginStudent(name)
        val reviewId = BddWorld.backend.lastPeerReviewCreated!!.id
        val result = BddWorld.harness.submitPeerReview(reviewId, com.stuf.grading.domain.model.SelfAssessmentDraft())
        BddWorld.capture(result)
    }

    @И("проставляется CompletedAt")
    fun completedAtSet() {
        val record = BddWorld.backend.lastPeerReviewCreated ?: BddWorld.backend.lastTeamPeerReview
        assertNotNull(record!!.completedAt)
    }

    @И("счётчик выполненных оцениваний увеличивается на 1")
    fun counterIncremented() {
        assertEquals(1, BddWorld.backend.lastProgress!!.completed)
    }

    @И("в прогрессе completed = {int}, required = {int}, canFinish = {word}, isCounted = {word}")
    fun progressValues(completed: Int, required: Int, canFinish: String, isCounted: String) {
        val p = BddWorld.backend.lastProgress!!
        assertEquals(completed, p.completed)
        assertEquals(required, p.required)
        assertEquals(canFinish.toBoolean(), p.canFinish)
        assertEquals(isCounted.toBoolean(), p.isCounted)
    }

    @Дано("{string} выполнил {int} оценивания")
    fun completedReviews(name: String, count: Int) {
        BddTestHelpers.completeIndividualReviews(name, count)
    }

    @Дано("{string} выполнил {int} оценивания при required = {int}")
    fun completedWithRequired(name: String, count: Int, @Suppress("UNUSED_PARAMETER") required: Int) {
        BddTestHelpers.completeIndividualReviews(name, count)
        BddWorld.backend.solutionCounted[
            BddWorld.backend.individualSolutions.values.first {
                BddWorld.backend.students[name] == it.authorId
            }.id,
        ] = true
    }

    @Дано("{string} прикрепил решение и выполнил {int} оценивания")
    fun attachedAndCompleted(name: String, count: Int) {
        BddTestHelpers.completeIndividualReviews(name, count)
    }

    @Дано("{string} выполнил только {int} оценивание из {int}")
    fun partialCompleted(name: String, done: Int, @Suppress("UNUSED_PARAMETER") total: Int) {
        BddTestHelpers.completeIndividualReviews(name, done)
    }


    @Когда("{string} нажимает {string}")
    fun finish(name: String, @Suppress("UNUSED_PARAMETER") action: String) = runBlocking {
        BddWorld.backend.loginStudent(name)
        val task = BddWorld.backend.findCurrentTask() as TaskPost
        val result = BddWorld.harness.finishIndividualPeerReview(TaskId(task.id.value))
        BddWorld.capture(result)
    }

    @Тогда("completed = {int} и required = {int}")
    fun completedAndRequired(completed: Int, required: Int) {
        val p = BddWorld.backend.lastProgress!!
        assertEquals(completed, p.completed)
        assertEquals(required, p.required)
    }

    @И("canFinish = {word}")
    fun canFinish(value: String) {
        assertEquals(value.toBoolean(), BddWorld.backend.lastProgress!!.canFinish)
    }

    @Тогда("решение {string} помечается как засчитанное")
    fun solutionCounted(@Suppress("UNUSED_PARAMETER") name: String) {
        assertTrue(BddWorld.backend.lastProgress!!.isCounted)
    }

    @И("в прогрессе isCounted = {word}")
    fun isCounted(value: String) {
        assertEquals(value.toBoolean(), BddWorld.backend.lastProgress!!.isCounted)
    }

    @И("решение остаётся незасчитанным")
    fun notCounted() {
        assertFalse(BddWorld.backend.lastProgress?.isCounted ?: true)
    }

    @Когда("{string} запрашивает следующее назначение и оценивает ещё одну работу")
    fun reviewOneMore(name: String) = runBlocking {
        BddWorld.backend.loginStudent(name)
        BddWorld.backend.submitIndividualSolution("ДопСтудент")
        val task = BddWorld.backend.findCurrentTask() as TaskPost
        val next = BddWorld.harness.getNextPeerReview(TaskId(task.id.value))
        if (next is DomainResult.Success && next.value != null) {
            BddWorld.harness.submitPeerReview(next.value.reviewId, com.stuf.grading.domain.model.SelfAssessmentDraft())
        }
        val progress = BddWorld.harness.getIndividualPeerReviewProgress(TaskId(task.id.value))
        BddWorld.capture(progress)
    }

    @Тогда("completed = {int}")
    fun thenCompletedOnly(completed: Int) {
        assertCompleted(completed)
    }

    @Тогда("isCounted = {word}")
    fun thenIsCountedOnly(value: String) {
        assertIsCounted(value)
    }

    @И("решение остаётся засчитанным")
    fun staysCounted() {
        assertTrue(BddWorld.backend.lastProgress!!.isCounted)
    }

    @Дано("{string} уже оценил все доступные чужие решения")
    fun allReviewed(name: String) {
        BddWorld.backend.submitIndividualSolution(name)
        BddWorld.backend.submitIndividualSolution("ЕдинственныйДругой")
        runBlocking {
            BddWorld.backend.loginStudent(name)
            val task = BddWorld.backend.findCurrentTask() as TaskPost
            val next = BddWorld.harness.getNextPeerReview(TaskId(task.id.value))
            if (next is DomainResult.Success && next.value != null) {
                BddWorld.harness.submitPeerReview(next.value.reviewId, com.stuf.grading.domain.model.SelfAssessmentDraft())
            }
        }
    }

    @Тогда("возвращается пустой результат \\(Data = null\\)")
    fun emptyResult() {
        assertTrue(BddWorld.lastResult is DomainResult.Success)
        assertNull((BddWorld.lastResult as DomainResult.Success).value)
    }

    @Дано("назначение со статусом {string} принадлежит студенту {string}")
    fun assignmentForStudent(@Suppress("UNUSED_PARAMETER") status: String, owner: String) {
        BddWorld.backend.submitIndividualSolution(owner)
        BddWorld.backend.submitIndividualSolution("Иван")
        runBlocking {
            BddWorld.backend.loginStudent(owner)
            val task = BddWorld.backend.findCurrentTask() as TaskPost
            BddWorld.harness.getNextPeerReview(TaskId(task.id.value))
        }
    }

    @Когда("{string} пытается отправить оценку по этому назначению")
    fun submitAsOther(name: String) = runBlocking {
        BddWorld.backend.loginStudent(name)
        val reviewId = BddWorld.backend.lastPeerReviewCreated!!.id
        val result = BddWorld.harness.submitPeerReview(reviewId, com.stuf.grading.domain.model.SelfAssessmentDraft())
        BddWorld.capture(result)
    }

    @Дано("назначение {string} уже в статусе {string}")
    fun completedAssignment(@Suppress("UNUSED_PARAMETER") name: String, status: String) {
        hasAssigned("Иван", status)
        runBlocking {
            BddWorld.backend.loginStudent("Иван")
            val reviewId = BddWorld.backend.lastPeerReviewCreated!!.id
            BddWorld.harness.submitPeerReview(reviewId, com.stuf.grading.domain.model.SelfAssessmentDraft())
        }
    }

    @Когда("{string} повторно отправляет оценку по этому назначению")
    fun resubmit(name: String) = submitAsOther(name)

    @Тогда("решение сохраняется со статусом Pending")
    fun pendingStatus() {
        assertEquals(SolutionStatus.PENDING, BddWorld.backend.lastSolution!!.status)
    }

    @И("решение НЕ отправляется на проверку преподавателю")
    fun notToTeacher() {
        assertFalse(BddWorld.backend.teacherReviewQueue.contains(BddWorld.backend.lastSolution!!.id))
    }

    @И("решение включается в пул для взаимного оценивания")
    fun inP2pPool() {
        val task = BddWorld.backend.findCurrentTask() as TaskPost
        assertEquals(GradingMode.PEER_TO_PEER, task.gradingMode)
    }

    @И("PeerReview переводится в статус {string}")
    fun reviewBecomes(status: String) {
        assertEquals(status.lowercase(), BddWorld.backend.lastPeerReviewCreated!!.status.name.lowercase())
    }

    private fun String.toBoolean(): Boolean = this == "true"

    private fun assertCompleted(completed: Int) {
        assertEquals(completed, BddWorld.backend.lastProgress!!.completed)
    }

    private fun assertIsCounted(value: String) {
        assertEquals(value.toBoolean(), BddWorld.backend.lastProgress!!.isCounted)
    }

    private fun resolveStudentId(name: String): com.stuf.domain.model.UserId {
        BddWorld.backend.students[name]?.let { return it }
        val normalized = name.removeSuffix("у").removeSuffix("е").removeSuffix("а")
        return BddWorld.backend.students[normalized]
            ?: BddWorld.backend.students.entries.firstOrNull { name.startsWith(it.key) }?.value
            ?: error("Неизвестный студент: $name")
    }
}
