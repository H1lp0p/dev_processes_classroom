package com.stuf.domain.repository

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.GradeBreakdown
import com.stuf.domain.model.Review
import com.stuf.domain.model.Solution
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.UserId
import com.stuf.grading.domain.model.SelfAssessmentDraft

interface SolutionRepository {
    suspend fun submitSolution(taskId: TaskId, text: String?, fileIds: List<String>): DomainResult<Solution>
    suspend fun cancelSolution(taskId: TaskId): DomainResult<Unit>
    suspend fun getUserSolution(taskId: TaskId): DomainResult<Solution?>
    suspend fun getTaskSolutions(
        taskId: TaskId,
        status: SolutionStatus? = null,
        studentId: UserId? = null,
    ): DomainResult<List<Solution>>

    suspend fun reviewSolution(solutionId: SolutionId, review: Review): DomainResult<Unit>

    /** Самооценка в составе PUT /api/task/{id}/solution. */
    suspend fun submitSelfAssessment(taskId: TaskId, draft: SelfAssessmentDraft): DomainResult<Unit>

    suspend fun deleteSelfAssessment(taskId: TaskId): DomainResult<Unit>

    suspend fun previewGrade(solutionId: SolutionId, draft: SelfAssessmentDraft): DomainResult<GradeBreakdown>
}
