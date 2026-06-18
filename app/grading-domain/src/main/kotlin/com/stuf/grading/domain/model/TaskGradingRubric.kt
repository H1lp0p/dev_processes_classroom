package com.stuf.grading.domain.model

/**
 * Агрегат конфигурации оценивания по заданию: критерии и политика задания (пороги, вес самооценки, штраф за дни).
 *
 * Идентификатор задания хранится строкой, чтобы модуль не зависел от UUID домена приложения.
 */
data class TaskGradingRubric(
    val taskId: String,
    val title: String,
    val assignmentMaxScore: Double,
    val criteria: List<CriterionDefinition>,
    /** Доля t ниже которой итог обнуляется (0..1). */
    val failThreshold: Double? = null,
    /** Доля t выше которой итог поднимается до [assignmentMaxScore] (0..1). */
    val successThreshold: Double? = null,
    /** Вес самооценки в смеси с оценкой преподавателя; 0 — самооценка отключена на уровне политики. */
    val studentScoreWeight: Double = 0.0,
    val penaltyPerDay: Double? = null,
    val maxPenaltyDays: Int = 0,
)
