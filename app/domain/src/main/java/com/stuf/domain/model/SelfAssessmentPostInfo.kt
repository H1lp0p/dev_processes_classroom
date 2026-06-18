package com.stuf.domain.model

import com.stuf.grading.domain.model.TaskGradingRubric
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Самооценка на посте задания: настроена ли рубрика (API) и открыта ли сейчас по дедлайну.
 *
 * Дедлайн самооценки совпадает с [TaskDetails.deadline] задания; после дедлайна доступ
 * зависит от [TaskPost.solvableAfterDeadline] / [TeamTaskPost.solvableAfterDeadline].
 */
data class SelfAssessmentPostInfo(
    val isConfigured: Boolean,
    val deadline: OffsetDateTime?,
    val isCurrentlyOpen: Boolean,
    val closedReason: String?,
)

fun TaskPost.selfAssessmentPostInfo(now: OffsetDateTime = OffsetDateTime.now()): SelfAssessmentPostInfo =
    buildSelfAssessmentPostInfo(
        gradingRubric = gradingRubric,
        deadline = taskDetails.deadline,
        solvableAfterDeadline = solvableAfterDeadline,
        now = now,
    )

fun TeamTaskPost.selfAssessmentPostInfo(now: OffsetDateTime = OffsetDateTime.now()): SelfAssessmentPostInfo =
    buildSelfAssessmentPostInfo(
        gradingRubric = gradingRubric,
        deadline = taskDetails.deadline,
        solvableAfterDeadline = solvableAfterDeadline,
        now = now,
    )

fun SelfAssessmentPostInfo.detailLines(
    formatter: DateTimeFormatter,
): List<String> {
    if (!isConfigured) {
        return listOf("Самооценка: не предусмотрена")
    }
    val lines = mutableListOf("Самооценка: предусмотрена")
    val deadlineText =
        deadline?.let { "Срок: ${it.format(formatter)} (совпадает со сроком сдачи)" }
            ?: "Срок: не задан"
    lines += deadlineText
    lines +=
        if (isCurrentlyOpen) {
            "Сейчас доступна"
        } else {
            closedReason ?: "Сейчас недоступна"
        }
    return lines
}

/** `null` — можно открыть экран самооценки; иначе текст вместо кнопки. */
fun SelfAssessmentPostInfo.navigationBlockMessage(
    hasSolution: Boolean,
    isTeamTask: Boolean,
    isSolutionChecked: Boolean,
): String? {
    if (!isConfigured) return null
    if (isSolutionChecked) {
        return "Самооценка недоступна: решение уже проверено."
    }
    if (!hasSolution) {
        return if (isTeamTask) {
            "Чтобы выставить самооценку, сначала капитан должен сдать решение команды."
        } else {
            "Чтобы выставить самооценку, сначала сдайте решение по заданию."
        }
    }
    if (!isCurrentlyOpen) {
        return closedReason ?: "Самооценка сейчас недоступна."
    }
    return null
}

fun SelfAssessmentPostInfo.canOpenSelfAssessmentScreen(
    hasSolution: Boolean,
    isTeamTask: Boolean,
    isSolutionChecked: Boolean,
): Boolean = navigationBlockMessage(hasSolution, isTeamTask, isSolutionChecked) == null

internal fun buildSelfAssessmentPostInfo(
    gradingRubric: TaskGradingRubric?,
    deadline: OffsetDateTime?,
    solvableAfterDeadline: Boolean?,
    now: OffsetDateTime,
): SelfAssessmentPostInfo {
    if (gradingRubric == null) {
        return SelfAssessmentPostInfo(
            isConfigured = false,
            deadline = deadline,
            isCurrentlyOpen = false,
            closedReason = null,
        )
    }
    val open = isSelfAssessmentOpen(deadline, solvableAfterDeadline, now)
    return SelfAssessmentPostInfo(
        isConfigured = true,
        deadline = deadline,
        isCurrentlyOpen = open,
        closedReason =
            if (open) {
                null
            } else {
                "Срок самооценки истёк"
            },
    )
}

internal fun isSelfAssessmentOpen(
    deadline: OffsetDateTime?,
    solvableAfterDeadline: Boolean?,
    now: OffsetDateTime,
): Boolean {
    if (deadline == null) return true
    if (!deadline.isBefore(now)) return true
    return solvableAfterDeadline == true
}
