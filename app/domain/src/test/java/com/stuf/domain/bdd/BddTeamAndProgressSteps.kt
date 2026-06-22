package com.stuf.domain.bdd

import com.stuf.domain.bdd.support.BddTestHelpers
import com.stuf.domain.bdd.support.BddWorld
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.GradingMode
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.TaskPost
import com.stuf.domain.model.TeamTaskPost
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

class BddTeamP2pSteps {

    @Дано("существует командное задание {string} с GradingMode = {string}")
    fun teamTask(title: String, mode: String) {
        BddWorld.backend.registerTeamTask(
            title,
            if (mode == "PeerToPeer") GradingMode.PEER_TO_PEER else GradingMode.TEACHER_REVIEW,
        )
    }

    @И("студент {string} состоит в команде {string}")
    fun studentInTeam(student: String, team: String) {
        BddWorld.backend.loginStudent(student)
        BddWorld.backend.joinTeam(student, team)
    }

    @И("существуют команды {string} и {string} с прикреплёнными решениями")
    fun teamsWithSolutions(teamB: String, teamC: String) {
        BddWorld.backend.submitTeamSolution(teamB)
        BddWorld.backend.submitTeamSolution(teamC)
    }

    @Дано("команда {string} прикрепила решение")
    fun teamSubmitted(team: String) {
        BddWorld.backend.submitTeamSolution(team)
    }

    @Дано("команда {string} не прикрепила решение")
    fun teamNotSubmitted(@Suppress("UNUSED_PARAMETER") team: String) {
        // нет решения команды
    }

    @Когда("{string} запрашивает список решений для оценки")
    fun listAvailable(name: String) = runBlocking {
        BddWorld.backend.loginStudent(name)
        val task = BddWorld.backend.findCurrentTask() as TeamTaskPost
        val result = BddWorld.harness.getAvailableTeamPeerReviews(TaskId(task.id.value))
        BddWorld.capture(result)
    }

    @Тогда("в списке присутствуют решения команд {string} и {string}")
    fun teamsInList(teamB: String, teamC: String) {
        val names = BddWorld.backend.lastAvailableTeamTargets.map { it.teamName }.toSet()
        assertTrue(names.contains(teamB))
        assertTrue(names.contains(teamC))
    }

    @И("решение команды {string} отсутствует в списке")
    fun teamAbsent(team: String) {
        val names = BddWorld.backend.lastAvailableTeamTargets.map { it.teamName }
        assertFalse(names.contains(team))
    }

    @Дано("{string} уже оценила решение команды {string}")
    @И("{string} оценила решение команды {string}")
    fun reviewedTeam(student: String, team: String) {
        BddWorld.backend.loginStudent(student)
        BddWorld.backend.submitTeamPeerReview(student, team)
    }

    @И("решение команды {string} помечено alreadyReviewed = true или отсутствует")
    fun alreadyReviewedOrMissing(team: String) {
        val target = BddWorld.backend.lastAvailableTeamTargets.firstOrNull { it.teamName == team }
        assertTrue(target == null || target.alreadyReviewed)
    }

    @И("решение команды {string} доступно для оценки")
    fun teamAvailable(team: String) {
        val target = BddWorld.backend.lastAvailableTeamTargets.first { it.teamName == team }
        assertFalse(target.alreadyReviewed)
    }

    @Когда("{string} оценивает решение команды {string} по критериям и подтверждает отправку")
    fun reviewTeam(student: String, team: String) = runBlocking {
        BddWorld.backend.loginStudent(student)
        val result = BddWorld.backend.submitTeamPeerReview(student, team)
        BddWorld.capture(result)
    }

    @Когда("{string} пытается оценить решение команды {string}")
    fun tryReviewTeam(student: String, team: String) = reviewTeam(student, team)

    @Когда("{string} повторно оценивает решение команды {string}")
    fun reviewAgain(student: String, team: String) = reviewTeam(student, team)

    @И("ReviewerTeam = команда {string}")
    fun reviewerTeam(team: String) {
        val teamId = BddWorld.backend.teams[team]
        assertEquals(teamId, BddWorld.backend.lastTeamPeerReview!!.reviewerTeamId)
    }

    @И("TeamSolution = решение команды {string}")
    fun teamSolution(team: String) {
        val teamId = BddWorld.backend.teams[team]!!
        val task = BddWorld.backend.findCurrentTask() as TeamTaskPost
        val solution = BddWorld.backend.teamSolutions[TaskId(task.id.value) to teamId]
        assertEquals(solution?.id, BddWorld.backend.lastTeamPeerReview!!.teamSolutionId)
    }

    @И("{string} оценила решение хотя бы одной другой команды")
    fun reviewedOther(student: String) {
        BddWorld.backend.submitTeamPeerReview(student, "B")
    }

    @И("{string} не оценила ни одной другой команды")
    fun noReviews(@Suppress("UNUSED_PARAMETER") student: String) {
        // нет завершённых peer review
    }

    @Когда("{string} оценивает решение первой другой команды")
    fun firstReview(student: String) = reviewTeam(student, "B")

    @Тогда("задание сразу засчитывается без отдельного действия {string}")
    fun autoCounted(@Suppress("UNUSED_PARAMETER") action: String) {
        assertTrue(BddWorld.backend.lastProgress!!.isCounted)
    }

    @Тогда("required = {int}, completed >= {int}, isCounted = {word}")
    fun teamProgress(required: Int, minCompleted: Int, isCounted: String) {
        val p = BddWorld.backend.lastProgress!!
        assertEquals(required, p.required)
        assertTrue(p.completed >= minCompleted)
        assertEquals(isCounted.toBoolean(), p.isCounted)
    }

    private fun String.toBoolean(): Boolean = this == "true"
}

class BddProgressAndRegressionSteps {

    @Дано("индивидуальное задание с GradingMode = {string} и MinPeerReviewsRequired = {int}")
    fun individualWithMin(mode: String, min: Int) {
        BddWorld.backend.registerIndividualTask(
            "Прогресс",
            if (mode == "PeerToPeer") GradingMode.PEER_TO_PEER else GradingMode.TEACHER_REVIEW,
            min,
        )
        BddWorld.backend.loginStudent("Студент")
    }

    @И("студент выполнил {int} оценивание")
    fun studentDidReviews(count: Int) {
        BddTestHelpers.completeIndividualReviews("Студент", count)
    }

    @Дано("командное задание с GradingMode = {string}")
    fun teamWithMode(mode: String) {
        BddWorld.backend.registerTeamTask(
            "Командный прогресс",
            if (mode == "PeerToPeer") GradingMode.PEER_TO_PEER else GradingMode.TEACHER_REVIEW,
        )
        BddWorld.backend.joinTeam("Студент", "A")
        BddWorld.backend.submitTeamSolution("B")
    }

    @И("команда прикрепила решение и студент оценил одну другую команду")
    fun teamSolutionAndReview() {
        BddWorld.backend.submitTeamSolution("A")
        BddWorld.backend.submitTeamPeerReview("Студент", "B")
    }

    @Дано("задание с GradingMode = {string}")
    fun taskWithMode(mode: String) {
        BddWorld.backend.registerIndividualTask(
            "Регресс",
            if (mode == "PeerToPeer") GradingMode.PEER_TO_PEER else GradingMode.TEACHER_REVIEW,
            if (mode == "PeerToPeer") 1 else null,
        )
        BddWorld.backend.loginStudent("Студент")
    }

    @Дано("задание с GradingMode = {string} и прикреплённое решение")
    fun taskWithSolution(mode: String) {
        taskWithMode(mode)
        BddWorld.backend.submitIndividualSolution("Студент")
    }

    @Когда("студент запрашивает детали своего решения")
    fun studentSolutionDetails() = runBlocking {
        val task = BddWorld.backend.findCurrentTask() as TaskPost
        val result = BddWorld.harness.getUserSolution(TaskId(task.id.value))
        BddWorld.capture(result)
    }

    @Когда("студент запрашивает детали решения команды")
    fun teamSolutionDetails() = runBlocking {
        BddWorld.backend.loginStudent("Студент")
        val result = BddWorld.backend.getTeamSolutionForStudent("Студент")
        BddWorld.capture(result)
    }

    @Когда("студент прикрепляет решение")
    fun studentSubmits() = runBlocking {
        val task = BddWorld.backend.findCurrentTask() as TaskPost
        val result = BddWorld.harness.submitSolution(TaskId(task.id.value), "t", emptyList())
        BddWorld.capture(result)
    }

    @Когда("студент запрашивает следующее назначение на P2P-оценку")
    fun studentRequestsP2p() = runBlocking {
        val task = BddWorld.backend.findCurrentTask() as TaskPost
        val result = BddWorld.harness.getNextPeerReview(TaskId(task.id.value))
        BddWorld.capture(result)
    }

    @Когда("^преподаватель отправляет оценку через /review$")
    fun teacherReviews() = runBlocking {
        val solution = BddWorld.backend.lastSolution ?: BddWorld.backend.individualSolutions.values.first()
        val result =
            BddWorld.harness.reviewSolution(
                solution.id,
                com.stuf.domain.model.Review(
                    score = com.stuf.domain.model.Score(9),
                    status = SolutionStatus.CHECKED,
                    comment = "ok",
                ),
            )
        BddWorld.capture(result)
    }

    @Тогда("присутствует блок peerReviewProgress")
    fun hasProgressBlock() {
        val progress =
            BddWorld.backend.lastSolution?.peerReviewProgress
                ?: BddWorld.backend.lastTeamSolution?.peerReviewProgress
        assertNotNull(progress)
        BddWorld.backend.lastProgress = progress
    }

    @И("required = {int}, completed = {int}, canFinish = {word}, isCounted = {word}")
    fun fullProgress(required: Int, completed: Int, canFinish: String, isCounted: String) {
        val p = BddWorld.backend.lastProgress!!
        assertEquals(required, p.required)
        assertEquals(completed, p.completed)
        assertEquals(canFinish.toBoolean(), p.canFinish)
        assertEquals(isCounted.toBoolean(), p.isCounted)
    }

    @И("required = {int}, isCounted = {word}")
    fun shortProgress(required: Int, isCounted: String) {
        val p = BddWorld.backend.lastProgress!!
        assertEquals(required, p.required)
        assertEquals(isCounted.toBoolean(), p.isCounted)
    }

    @Тогда("блок peerReviewProgress отсутствует \\(null\\)")
    fun noProgress() {
        assertNull(BddWorld.backend.lastSolution?.peerReviewProgress)
    }

    @И("поля Score и TeacherEvaluation работают как раньше")
    fun teacherFieldsWork() {
        val solution = BddWorld.backend.lastSolution
        assertNotNull(solution)
        assertNull(solution!!.peerReviewProgress)
        assertEquals(SolutionStatus.PENDING, solution.status)
    }

    @Тогда("решение ожидает проверки преподавателем")
    fun awaitsTeacher() {
        assertTrue(BddWorld.backend.teacherReviewQueue.isNotEmpty())
    }

    @И("P2P-назначения не создаются")
    fun noP2pAssignments() {
        assertTrue(BddWorld.backend.peerReviews.isEmpty())
    }

    @Тогда("решение получает статус Checked и итоговый балл как прежде")
    fun checkedWithScore() {
        assertEquals(SolutionStatus.CHECKED, BddWorld.backend.lastSolution!!.status)
        assertNotNull(BddWorld.backend.lastSolution!!.score)
    }

    private fun String.toBoolean(): Boolean = this == "true"
}
