package com.stuf.data.demo

import com.stuf.grading.domain.model.CriterionDefinition
import com.stuf.grading.domain.model.CriterionDirection
import com.stuf.grading.domain.model.CriterionId
import com.stuf.grading.domain.model.TaskGradingRubric
import kotlin.math.abs

/**
 * Рубрики для offline-демо (UUID постов синхронизированы с [DemoIds]).
 */
internal object DemoGradingRubrics {
    fun rubricForPost(postId: String): TaskGradingRubric {
        val id = postId.trim().lowercase()
        return when (id) {
            DemoIds.postHomework.value.toString().lowercase() -> rubricHomework(id)
            DemoIds.postWebLab.value.toString().lowercase() -> rubricWebLab(id)
            DemoIds.postTeamAlgebra.value.toString().lowercase() -> rubricTeamAlgebra(id)
            DemoIds.postTeamWebSprint.value.toString().lowercase() -> rubricTeamWebSprint(id)
            DemoIds.postTeamWebCaptainDraft.value.toString().lowercase() -> rubricTeamCaptainDraft(id)
            DemoIds.postTeamOverdue.value.toString().lowercase() -> rubricTeamOverdue(id)
            DemoIds.postTeamMobilePartial.value.toString().lowercase() -> rubricTeamMobilePartial(id)
            DemoIds.postMobileLab.value.toString().lowercase() -> rubricMobileLab(id)
            DemoIds.postTeamMobileNoSa.value.toString().lowercase() -> rubricDisabled(id)
            else -> rubricGenericUnknown(id)
        }
    }

    private fun rubricDisabled(taskId: String): TaskGradingRubric =
        rubricHomework(taskId).copy(studentScoreWeight = 0.0)

    private fun rubricHomework(taskId: String): TaskGradingRubric =
        TaskGradingRubric(
            taskId = taskId,
            title = "Самооценка: домашняя работа №1",
            assignmentMaxScore = 5.0,
            failThreshold = 0.2,
            successThreshold = 0.9,
            studentScoreWeight = 0.15,
            penaltyPerDay = 0.25,
            maxPenaltyDays = 7,
            criteria =
                listOf(
                    weighted(taskId, "hw-solution", "Правильность решения", 5.0, 0.65),
                    weighted(taskId, "hw-writeup", "Пояснение хода решения", 3.0, 0.35),
                    bonus(taskId, "hw-bonus", "Альтернативный способ", 0.5),
                    penalty(taskId, "hw-pen", "Нарушен формат сдачи", 0.5),
                    quality(taskId, "hw-q", "Высокая доля — микробонус", 0.75, 0.25),
                ),
        )

    private fun rubricWebLab(taskId: String): TaskGradingRubric =
        TaskGradingRubric(
            taskId = taskId,
            title = "Самооценка: лабораторная",
            assignmentMaxScore = 10.0,
            failThreshold = 0.1,
            successThreshold = 0.92,
            studentScoreWeight = 0.2,
            penaltyPerDay = 0.3,
            maxPenaltyDays = 10,
            criteria =
                listOf(
                    weighted(taskId, "lab-markup", "HTML", 6.0, 0.5),
                    weighted(taskId, "lab-css", "CSS", 6.0, 0.35),
                    weighted(taskId, "lab-a11y", "Контраст", 4.0, 0.15),
                    bonus(taskId, "lab-bonus", "Адаптив", 1.0),
                    blocking(taskId, "lab-block", "Плагиат макета", 4.0),
                ),
        )

    private fun rubricTeamAlgebra(taskId: String): TaskGradingRubric =
        TaskGradingRubric(
            taskId = taskId,
            title = "Самооценка: командный проект (алгебра)",
            assignmentMaxScore = 10.0,
            failThreshold = 0.18,
            successThreshold = 0.88,
            studentScoreWeight = 0.35,
            penaltyPerDay = 0.4,
            maxPenaltyDays = 5,
            criteria =
                listOf(
                    weighted(taskId, "ta-research", "Исследование", 6.0, 0.55),
                    weighted(taskId, "ta-report", "Отчёт", 5.0, 0.3),
                    weighted(taskId, "ta-teamwork", "Вклад в команду", 4.0, 0.15),
                    bonus(taskId, "ta-bonus", "Доп. источники", 1.0),
                    penalty(taskId, "ta-pen", "Срыв этапов", 1.0),
                    quality(taskId, "ta-q", "Низкая доля — штраф", 0.35, 0.75, subtract = true),
                    blocking(taskId, "ta-block", "Плагиат", 2.0),
                ),
        )

    private fun rubricTeamWebSprint(taskId: String): TaskGradingRubric =
        TaskGradingRubric(
            taskId = taskId,
            title = "Самооценка: UI-спринт",
            assignmentMaxScore = 8.0,
            failThreshold = 0.12,
            successThreshold = 0.95,
            studentScoreWeight = 0.3,
            penaltyPerDay = 0.2,
            maxPenaltyDays = 4,
            criteria =
                listOf(
                    weighted(taskId, "tws-ui", "Макет", 5.0, 0.5),
                    weighted(taskId, "tws-ux", "Сценарии", 5.0, 0.35),
                    weighted(taskId, "tws-polish", "Полировка", 3.0, 0.15),
                    bonus(taskId, "tws-bonus", "Кликабельный прототип", 0.75),
                    penalty(taskId, "tws-pen", "Без согласования", 0.5),
                    quality(taskId, "tws-q", "Доля > 0.8 — бонус", 0.8, 0.4),
                ),
        )

    private fun rubricTeamCaptainDraft(taskId: String): TaskGradingRubric =
        TaskGradingRubric(
            taskId = taskId,
            title = "Самооценка: черновик UI",
            assignmentMaxScore = 10.0,
            studentScoreWeight = 0.4,
            criteria =
                listOf(
                    weighted(taskId, "draft-scope", "Покрытие экранов", 5.0, 0.6),
                    weighted(taskId, "draft-clarity", "Понятность", 5.0, 0.4),
                    bonus(taskId, "draft-bonus", "Вопросы к ревью", 0.5),
                ),
        )

    private fun rubricTeamOverdue(taskId: String): TaskGradingRubric =
        TaskGradingRubric(
            taskId = taskId,
            title = "Самооценка: просрочка",
            assignmentMaxScore = 5.0,
            failThreshold = 0.25,
            successThreshold = 0.85,
            studentScoreWeight = 0.1,
            penaltyPerDay = 1.0,
            maxPenaltyDays = 3,
            criteria =
                listOf(
                    weighted(taskId, "ov-done", "Объём до дедлайна", 5.0, 0.7),
                    weighted(taskId, "ov-comm", "Коммуникация", 3.0, 0.3),
                    penalty(taskId, "ov-pen", "Критичный баг", 2.0),
                    blocking(taskId, "ov-block", "Нарушение сроков", 1.0),
                ),
        )

    private fun rubricTeamMobilePartial(taskId: String): TaskGradingRubric =
        TaskGradingRubric(
            taskId = taskId,
            title = "Самооценка: мобильный модуль (частично сдано)",
            assignmentMaxScore = 10.0,
            studentScoreWeight = 0.25,
            criteria =
                listOf(
                    weighted(taskId, "mob-arch", "Архитектура", 5.0, 0.5),
                    weighted(taskId, "mob-ui", "UI слой", 5.0, 0.5),
                    bonus(taskId, "mob-bonus", "Тесты", 1.0),
                ),
        )

    private fun rubricMobileLab(taskId: String): TaskGradingRubric =
        rubricWebLab(taskId).copy(
            taskId = taskId,
            title = "Самооценка: лабораторная (мобильный курс)",
            studentScoreWeight = 0.2,
        )

    private fun rubricGenericUnknown(taskId: String): TaskGradingRubric {
        val n = abs(taskId.hashCode()) % 3
        val max = 10.0 - n * 2
        return TaskGradingRubric(
            taskId = taskId,
            title = "Самооценка: задание",
            assignmentMaxScore = max,
            studentScoreWeight = 0.2,
            criteria =
                listOf(
                    weighted(taskId, "gen-a", "Постановка", max, 0.6),
                    weighted(taskId, "gen-b", "Качество", max, 0.4),
                    bonus(taskId, "gen-bonus", "Инициатива", 0.5),
                ),
        )
    }

    private fun weighted(
        taskId: String,
        suffix: String,
        title: String,
        max: Double,
        weight: Double,
    ) = CriterionDefinition.Weighted(CriterionId("$taskId-$suffix"), title, max, weight)

    private fun bonus(taskId: String, suffix: String, title: String, points: Double) =
        CriterionDefinition.ManualBonusPenalty(
            CriterionId("$taskId-$suffix"),
            title,
            points,
            CriterionDirection.ADD,
        )

    private fun penalty(taskId: String, suffix: String, title: String, points: Double) =
        CriterionDefinition.ManualBonusPenalty(
            CriterionId("$taskId-$suffix"),
            title,
            points,
            CriterionDirection.SUBTRACT,
        )

    private fun quality(
        taskId: String,
        suffix: String,
        title: String,
        threshold: Double,
        score: Double,
        subtract: Boolean = false,
    ) = CriterionDefinition.QualityCoefficient(
        CriterionId("$taskId-$suffix"),
        title,
        threshold,
        score,
        if (subtract) CriterionDirection.SUBTRACT else CriterionDirection.ADD,
    )

    private fun blocking(taskId: String, suffix: String, title: String, cap: Double) =
        CriterionDefinition.Blocking(CriterionId("$taskId-$suffix"), title, cap)
}
