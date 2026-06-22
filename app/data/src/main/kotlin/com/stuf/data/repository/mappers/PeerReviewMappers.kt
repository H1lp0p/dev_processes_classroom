package com.stuf.data.repository.mappers

import com.stuf.data.model.AnonymizedSolutionDto
import com.stuf.data.model.GradingMode as ApiGradingMode
import com.stuf.data.model.PeerReviewProgressDto
import com.stuf.data.model.PeerReviewTargetDto
import com.stuf.data.model.PeerReviewTeamTargetDto
import com.stuf.data.model.SubmitPeerReviewDto
import com.stuf.domain.model.AnonymizedSolution
import com.stuf.domain.model.FileInfo
import com.stuf.domain.model.GradingMode
import com.stuf.domain.model.PeerReviewId
import com.stuf.domain.model.PeerReviewProgress
import com.stuf.domain.model.PeerReviewTarget
import com.stuf.domain.model.PeerReviewTeamTarget
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TaskId
import com.stuf.grading.domain.model.SelfAssessmentDraft

internal fun ApiGradingMode?.toDomain(): GradingMode =
    when (this) {
        ApiGradingMode.PeerToPeer -> GradingMode.PEER_TO_PEER
        ApiGradingMode.TeacherReview, null -> GradingMode.TEACHER_REVIEW
    }

internal fun GradingMode.toApi(): ApiGradingMode =
    when (this) {
        GradingMode.TEACHER_REVIEW -> ApiGradingMode.TeacherReview
        GradingMode.PEER_TO_PEER -> ApiGradingMode.PeerToPeer
    }

internal fun PeerReviewProgressDto.toDomain(): PeerReviewProgress =
    PeerReviewProgress(
        required = required,
        completed = completed,
        canFinish = canFinish,
        isCounted = isCounted,
    )

internal fun PeerReviewTargetDto.toDomain(): PeerReviewTarget =
    PeerReviewTarget(
        reviewId = PeerReviewId(reviewId),
        taskId = TaskId(taskId),
        solution = solution.toDomain(),
        criteria = criteria.mapNotNull { it.toCriterionDefinition() },
        assignedAt = assignedAt,
    )

internal fun AnonymizedSolutionDto.toDomain(): AnonymizedSolution =
    AnonymizedSolution(
        text = text,
        files =
            files.orEmpty().map { file ->
                FileInfo(id = file.id ?: "", name = file.name ?: "")
            },
    )

internal fun PeerReviewTeamTargetDto.toDomain(): PeerReviewTeamTarget =
    PeerReviewTeamTarget(
        teamSolutionId = SolutionId(teamSolutionId),
        teamName = teamName,
        submittedAt = submittedAt,
        alreadyReviewed = alreadyReviewed,
    )

internal fun SelfAssessmentDraft.toSubmitPeerReviewDto(): SubmitPeerReviewDto =
    SubmitPeerReviewDto(evaluation = toEvaluationDto())
