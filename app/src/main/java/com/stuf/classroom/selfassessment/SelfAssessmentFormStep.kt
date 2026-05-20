package com.stuf.classroom.selfassessment

import com.stuf.grading.domain.model.CriterionDefinition
import com.stuf.grading.domain.model.TaskGradingRubric

/**
 * Один шаг многоступенчатой формы самооценки (до экрана результата).
 */
sealed class SelfAssessmentFormStep {
    /** Один весовой критерий — отдельный экран. */
    data class Weighted(
        val criterion: CriterionDefinition.Weighted,
    ) : SelfAssessmentFormStep()

    /** Ручные бонусы и штрафы. */
    data object BonusPenaltySection : SelfAssessmentFormStep()

    companion object {
        fun build(rubric: TaskGradingRubric): List<SelfAssessmentFormStep> {
            val steps = mutableListOf<SelfAssessmentFormStep>()
            rubric.criteria
                .filterIsInstance<CriterionDefinition.Weighted>()
                .forEach { steps.add(Weighted(it)) }
            if (rubric.criteria.any { it is CriterionDefinition.ManualBonusPenalty }) {
                steps.add(BonusPenaltySection)
            }
            return steps
        }
    }
}
