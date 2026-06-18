package com.stuf.grading.domain.preview

import com.stuf.grading.domain.model.CriterionDefinition
import com.stuf.grading.domain.model.CriterionDirection
import com.stuf.grading.domain.model.CriterionId
import com.stuf.grading.domain.model.SelfAssessmentDraft
import com.stuf.grading.domain.model.TaskGradingRubric
import kotlin.math.min

/**
 * Чистый расчёт превью по черновику самооценки (весовые + ручные тумблеры + качество + блокировка + пороги).
 * Порядок и семантика согласованы с диаграммой bounded context «критерии оценивания».
 *
 * Штраф за дни просрочки здесь не учитывается (нет даты сдачи в черновике); добавится при интеграции с API.
 */
object RubricPreviewEngine {

    fun preview(
        rubric: TaskGradingRubric,
        draft: SelfAssessmentDraft,
    ): RubricPreviewResult {
        val max = rubric.assignmentMaxScore.takeIf { it > 0 } ?: 1.0

        val weightedContributions = ArrayList<WeightedContribution>()
        var weightedSum = 0.0
        for (c in rubric.criteria) {
            if (c is CriterionDefinition.Weighted) {
                val raw = draft.weightedScores[c.id]?.coerceIn(0.0, c.maxScore) ?: 0.0
                val contrib = raw * c.weight
                weightedContributions +=
                    WeightedContribution(
                        criterionId = c.id,
                        title = c.title,
                        rawScore = raw,
                        weight = c.weight,
                        contribution = contrib,
                    )
                weightedSum += contrib
            }
        }

        var manualDelta = 0.0
        for (c in rubric.criteria) {
            if (c is CriterionDefinition.ManualBonusPenalty) {
                val on = draft.toggledEnabled[c.id] == true
                if (!on) continue
                manualDelta +=
                    when (c.direction) {
                        CriterionDirection.ADD -> c.points
                        CriterionDirection.SUBTRACT -> -c.points
                    }
            }
        }

        var score = weightedSum + manualDelta

        var qualityDelta = 0.0
        for (c in rubric.criteria) {
            if (c !is CriterionDefinition.QualityCoefficient) continue
            val t = if (max > 0) score / max else 0.0
            val applies =
                when (c.direction) {
                    CriterionDirection.ADD -> t > c.threshold
                    CriterionDirection.SUBTRACT -> t < c.threshold
                }
            if (applies) {
                val delta =
                    when (c.direction) {
                        CriterionDirection.ADD -> c.score
                        CriterionDirection.SUBTRACT -> -c.score
                    }
                qualityDelta += delta
                score += delta
            }
        }

        val afterModifiers = score

        var afterBlocking = afterModifiers
        for (c in rubric.criteria) {
            if (c is CriterionDefinition.Blocking) {
                val on = draft.toggledEnabled[c.id] == true
                if (on) {
                    afterBlocking = min(afterBlocking, c.maxAllowedScoreWhenActive)
                }
            }
        }

        val tFinal = if (max > 0) afterBlocking / max else 0.0
        var final = afterBlocking
        var zeroed = false
        var boosted = false

        val fail = rubric.failThreshold
        if (fail != null && tFinal < fail) {
            final = 0.0
            zeroed = true
        }

        val success = rubric.successThreshold
        if (!zeroed && success != null && tFinal > success) {
            final = max
            boosted = true
        }

        final = final.coerceIn(0.0, max)

        return RubricPreviewResult(
            weightedContributions = weightedContributions,
            manualToggleDelta = manualDelta,
            qualityDelta = qualityDelta,
            scoreAfterModifiers = afterModifiers,
            scoreAfterBlocking = afterBlocking,
            finalScore = final,
            zeroedByFailThreshold = zeroed,
            boostedToMaxBySuccessThreshold = boosted,
        )
    }

    fun defaultDraft(rubric: TaskGradingRubric): SelfAssessmentDraft {
        val weighted =
            rubric.criteria
                .filterIsInstance<CriterionDefinition.Weighted>()
                .associate { it.id to 0.0 }
        val toggles =
            rubric.criteria
                .filter { it is CriterionDefinition.ManualBonusPenalty || it is CriterionDefinition.Blocking }
                .associate { it.id to false }
        return SelfAssessmentDraft(weightedScores = weighted, toggledEnabled = toggles)
    }
}
