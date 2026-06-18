package com.stuf.domain.model

/**
 * Разбор итоговой оценки (ответ preview / breakdown с API).
 */
data class GradeBreakdown(
    val baseTeacherScore: Double,
    val baseStudentScore: Double?,
    val baseScore: Double,
    val afterQualityCoefficient: Double,
    val latePenalty: Double,
    val afterLatePenalty: Double,
    val afterBlocking: Double,
    val finalScore: Double,
    val expiredDays: Int,
    val thresholdApplied: Boolean,
    val thresholdReason: String?,
)
