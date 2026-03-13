package com.stuf.domain.usecase.impl

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Review
import com.stuf.domain.model.Solution
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.SolutionRepository
import com.stuf.domain.usecase.CancelSolution
import com.stuf.domain.usecase.GetSolutionsForTask
import com.stuf.domain.usecase.GetUserSolution
import com.stuf.domain.usecase.ReviewSolution
import com.stuf.domain.usecase.SubmitSolution
import com.stuf.domain.usecase.UpdateSolution
import javax.inject.Inject

class SubmitSolutionUseCase @Inject constructor(
    private val repository: SolutionRepository,
) : SubmitSolution {

    override suspend fun invoke(
        taskId: TaskId,
        text: String?,
        fileIds: List<String>,
    ): DomainResult<Solution> {
        val isTextBlank = text?.isBlank() == true
        val hasFiles = fileIds.isNotEmpty()

        if (isTextBlank && !hasFiles) {
            return DomainResult.Failure(DomainError.Validation("Solution must have text or files"))
        }

        return repository.submitSolution(
            taskId = taskId,
            text = if (isTextBlank) null else text,
            fileIds = fileIds,
        )
    }
}

class UpdateSolutionUseCase @Inject constructor(
    private val repository: SolutionRepository,
) : UpdateSolution {

    override suspend fun invoke(
        taskId: TaskId,
        text: String?,
        fileIds: List<String>,
    ): DomainResult<Solution> {
        val isTextBlank = text?.isBlank() == true
        val hasFiles = fileIds.isNotEmpty()

        if (isTextBlank && !hasFiles) {
            return DomainResult.Failure(DomainError.Validation("Solution must have text or files"))
        }

        return repository.submitSolution(
            taskId = taskId,
            text = if (isTextBlank) null else text,
            fileIds = fileIds,
        )
    }
}

class CancelSolutionUseCase @Inject constructor(
    private val repository: SolutionRepository,
) : CancelSolution {

    override suspend fun invoke(taskId: TaskId): DomainResult<Unit> {
        return repository.cancelSolution(taskId)
    }
}

class GetSolutionsForTaskUseCase @Inject constructor(
    private val repository: SolutionRepository,
) : GetSolutionsForTask {

    override suspend fun invoke(
        taskId: TaskId,
        status: SolutionStatus?,
        studentId: UserId?,
    ): DomainResult<List<Solution>> {
        return repository.getTaskSolutions(taskId, status, studentId)
    }
}

class ReviewSolutionUseCase @Inject constructor(
    private val repository: SolutionRepository,
) : ReviewSolution {

    override suspend fun invoke(
        solutionId: SolutionId,
        review: Review,
    ): DomainResult<Unit> {
        return repository.reviewSolution(solutionId, review)
    }
}

class GetUserSolutionUseCase @Inject constructor(
    private val repository: SolutionRepository,
) : GetUserSolution {

    override suspend fun invoke(taskId: TaskId): DomainResult<Solution?> {
        return repository.getUserSolution(taskId)
    }
}

