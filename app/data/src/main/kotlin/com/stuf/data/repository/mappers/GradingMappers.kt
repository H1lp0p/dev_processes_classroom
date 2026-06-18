package com.stuf.data.repository.mappers

import com.stuf.data.model.CriterionDirection as ApiCriterionDirection
import com.stuf.data.model.CriterionDto
import com.stuf.data.model.CriterionTypeDto
import com.stuf.data.model.EvaluationDto
import com.stuf.data.model.GradeBreakdownDto
import com.stuf.data.model.GradePreviewRequestDto
import com.stuf.data.model.MemberSelfAssessmentDto
import com.stuf.data.model.PostDetailsDto
import com.stuf.data.model.SubmitSelfAssessmentDto
import com.stuf.data.model.ToggledValueDto
import com.stuf.data.model.WeightedValueDto
import com.stuf.domain.model.GradeBreakdown
import com.stuf.domain.model.MemberSelfAssessment
import com.stuf.domain.model.UserId
import com.stuf.grading.domain.model.CriterionDefinition
import com.stuf.grading.domain.model.CriterionDirection
import com.stuf.grading.domain.model.CriterionId
import com.stuf.grading.domain.model.SelfAssessmentDraft
import com.stuf.grading.domain.model.TaskGradingRubric
import java.util.UUID

internal fun PostDetailsDto.toTaskGradingRubric(): TaskGradingRubric? {
    val postId = id?.toString() ?: return null
    val weight = studentScoreWeight?.toDouble() ?: 0.0
    val defs = criteria.orEmpty().mapNotNull { it.toCriterionDefinition() }
    if (weight <= 0.0 || defs.isEmpty()) return null
    return TaskGradingRubric(
        taskId = postId,
        title = title,
        assignmentMaxScore = (maxScore ?: 5).toDouble(),
        criteria = defs,
        failThreshold = failThreshold?.toDouble(),
        successThreshold = successThreshold?.toDouble(),
        studentScoreWeight = weight,
        penaltyPerDay = penaltyPerDay?.toDouble(),
        maxPenaltyDays = maxDays ?: 0,
    )
}

internal fun CriterionDto.toCriterionDefinition(): CriterionDefinition? {
    val criterionId =
        CriterionId(
            id?.toString() ?: "criterion-order-${orderIndex ?: 0}",
        )
    val criterionTitle = title ?: "Критерий"
    return when (type) {
        CriterionTypeDto.weighted ->
            CriterionDefinition.Weighted(
                id = criterionId,
                title = criterionTitle,
                maxScore = maxScore?.toDouble() ?: 0.0,
                weight = weight?.toDouble() ?: 0.0,
            )
        CriterionTypeDto.bonusPenalty ->
            CriterionDefinition.ManualBonusPenalty(
                id = criterionId,
                title = criterionTitle,
                points = score?.toDouble() ?: 0.0,
                direction = direction.toDomain(),
            )
        CriterionTypeDto.quality ->
            CriterionDefinition.QualityCoefficient(
                id = criterionId,
                title = criterionTitle,
                threshold = threshold?.toDouble() ?: 0.0,
                score = this.score?.toDouble() ?: 0.0,
                direction = direction.toDomain(),
            )
        CriterionTypeDto.blocking ->
            CriterionDefinition.Blocking(
                id = criterionId,
                title = criterionTitle,
                maxAllowedScoreWhenActive = maxAllowedScore?.toDouble() ?: 0.0,
            )
        null -> null
    }
}

private fun ApiCriterionDirection?.toDomain(): CriterionDirection =
    when (this) {
        ApiCriterionDirection.subtract -> CriterionDirection.SUBTRACT
        ApiCriterionDirection.add, null -> CriterionDirection.ADD
    }

internal fun EvaluationDto.toSelfAssessmentDraft(): SelfAssessmentDraft {
    val weighted =
        weightedValues.orEmpty().associate {
            CriterionId(it.criterionId.toString()) to (it.score?.toDouble() ?: 0.0)
        }
    val toggles =
        toggledValues.orEmpty().associate {
            CriterionId(it.criterionId.toString()) to (it.enabled == true)
        }
    return SelfAssessmentDraft(weightedScores = weighted, toggledEnabled = toggles)
}

internal fun SelfAssessmentDraft.toEvaluationDto(): EvaluationDto =
    EvaluationDto(
        weightedValues =
            weightedScores.map { (id, score) ->
                WeightedValueDto(
                    criterionId = UUID.fromString(id.value),
                    score = score.toFloat(),
                )
            },
        toggledValues =
            toggledEnabled.map { (id, enabled) ->
                ToggledValueDto(
                    criterionId = UUID.fromString(id.value),
                    enabled = enabled,
                )
            },
    )

internal fun SelfAssessmentDraft.toSubmitSelfAssessmentDto(): SubmitSelfAssessmentDto =
    SubmitSelfAssessmentDto(evaluation = toEvaluationDto())

internal fun SelfAssessmentDraft.toGradePreviewRequestDto(): GradePreviewRequestDto =
    GradePreviewRequestDto(evaluation = toEvaluationDto())

internal fun MemberSelfAssessmentDto.toDomain(): MemberSelfAssessment =
    MemberSelfAssessment(
        userId = UserId(checkNotNull(userId)),
        credentials = credentials,
        evaluation = evaluation?.toSelfAssessmentDraft(),
    )

internal fun GradeBreakdownDto.toDomain(): GradeBreakdown =
    GradeBreakdown(
        baseTeacherScore = baseTeacherScore?.toDouble() ?: 0.0,
        baseStudentScore = baseStudentScore?.toDouble(),
        baseScore = baseScore?.toDouble() ?: 0.0,
        afterQualityCoefficient = afterQualityCoefficient?.toDouble() ?: 0.0,
        latePenalty = latePenalty?.toDouble() ?: 0.0,
        afterLatePenalty = afterLatePenalty?.toDouble() ?: 0.0,
        afterBlocking = afterBlocking?.toDouble() ?: 0.0,
        finalScore = finalScore?.toDouble() ?: 0.0,
        expiredDays = expiredDays ?: 0,
        thresholdApplied = thresholdApplied == true,
        thresholdReason = thresholdReason,
    )
