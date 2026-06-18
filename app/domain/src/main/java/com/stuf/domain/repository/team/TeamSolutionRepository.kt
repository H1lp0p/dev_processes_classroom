package com.stuf.domain.repository

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.GradeBreakdown
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.TeamTaskSolution
import com.stuf.grading.domain.model.SelfAssessmentDraft

interface TeamSolutionRepository {
    suspend fun getTeamSolution(taskId: TaskId): DomainResult<TeamTaskSolution?>

    suspend fun submitTeamSolution(
        taskId: TaskId,
        text: String?,
        fileIds: List<String>,
        selfAssessment: SelfAssessmentDraft? = null,
    ): DomainResult<SolutionId>

    suspend fun deleteTeamSolution(taskId: TaskId): DomainResult<Unit>

    suspend fun submitSelfAssessment(taskId: TaskId, draft: SelfAssessmentDraft): DomainResult<Unit>

    suspend fun deleteSelfAssessment(taskId: TaskId): DomainResult<Unit>

    suspend fun previewTeamGrade(solutionId: SolutionId, draft: SelfAssessmentDraft): DomainResult<GradeBreakdown>
}
