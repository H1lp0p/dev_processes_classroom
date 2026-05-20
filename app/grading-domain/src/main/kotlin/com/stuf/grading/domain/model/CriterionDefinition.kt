package com.stuf.grading.domain.model

/**
 * Определение критерия в шаблоне оценивания задания (bounded context «критерии оценивания»).
 */
sealed class CriterionDefinition {
    abstract val id: CriterionId
    abstract val title: String

    /** Весовой критерий: ввод балла 0..maxScore, умножается на weight. */
    data class Weighted(
        override val id: CriterionId,
        override val title: String,
        val maxScore: Double,
        val weight: Double,
    ) : CriterionDefinition()

    /**
     * Ручной бонус или штраф: тумблер; при включении добавляет или вычитает фиксированные баллы.
     */
    data class ManualBonusPenalty(
        override val id: CriterionId,
        override val title: String,
        val points: Double,
        val direction: CriterionDirection,
    ) : CriterionDefinition()

    /**
     * Коэффициент качества: автоматически от доли t = currentScore / [assignmentMaxScore]
     * (см. заметку на диаграмме).
     */
    data class QualityCoefficient(
        override val id: CriterionId,
        override val title: String,
        /** Порог для доли набранных баллов, 0..1. */
        val threshold: Double,
        val score: Double,
        val direction: CriterionDirection,
    ) : CriterionDefinition()

    /**
     * Блокирующий модификатор: при включении итог не выше заданного потолка.
     */
    data class Blocking(
        override val id: CriterionId,
        override val title: String,
        val maxAllowedScoreWhenActive: Double,
    ) : CriterionDefinition()
}
