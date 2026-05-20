package com.stuf.grading.domain.preview

import com.stuf.grading.domain.model.CriterionId

/**
 * Разложение для UI: вклад строк и итог после модификаторов (упрощённый клиентский превью).
 */
data class RubricPreviewResult(
    val weightedContributions: List<WeightedContribution>,
    val manualToggleDelta: Double,
    val qualityDelta: Double,
    val scoreAfterModifiers: Double,
    val scoreAfterBlocking: Double,
    val finalScore: Double,
    val zeroedByFailThreshold: Boolean,
    val boostedToMaxBySuccessThreshold: Boolean,
)

data class WeightedContribution(
    val criterionId: CriterionId,
    val title: String,
    val rawScore: Double,
    val weight: Double,
    /** rawScore * weight */
    val contribution: Double,
)
