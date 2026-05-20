package com.stuf.grading.domain.model

/**
 * Черновик ввода самооценки: баллы по весовым критериям и состояние тумблеров.
 */
data class SelfAssessmentDraft(
    val weightedScores: Map<CriterionId, Double> = emptyMap(),
    val toggledEnabled: Map<CriterionId, Boolean> = emptyMap(),
)
