package com.stuf.domain.usecase.impl

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.TaskPost
import com.stuf.domain.model.TeamTaskPost
import com.stuf.domain.repository.PostRepository
import com.stuf.domain.repository.SolutionRepository
import com.stuf.domain.repository.TeamSolutionRepository
import com.stuf.domain.usecase.DeleteIndividualSelfAssessment
import com.stuf.domain.usecase.DeleteTeamSelfAssessment
import com.stuf.domain.usecase.GetTaskGradingRubric
import com.stuf.domain.usecase.PreviewIndividualSelfAssessment
import com.stuf.domain.usecase.PreviewTeamSelfAssessment
import com.stuf.domain.usecase.SubmitIndividualSelfAssessment
import com.stuf.domain.usecase.SubmitTeamSelfAssessment
import com.stuf.grading.domain.model.SelfAssessmentDraft
import com.stuf.grading.domain.model.TaskGradingRubric
import javax.inject.Inject

class GetTaskGradingRubricUseCase @Inject constructor(
    private val postRepository: PostRepository,
) : GetTaskGradingRubric {

    override suspend fun invoke(postId: PostId): DomainResult<TaskGradingRubric?> =
        when (val result = postRepository.getPost(postId)) {
            is DomainResult.Success -> DomainResult.Success(result.value.gradingRubricOrNull())
            is DomainResult.Failure -> result
        }
}

private fun Post.gradingRubricOrNull(): TaskGradingRubric? =
    when (this) {
        is TaskPost -> gradingRubric
        is TeamTaskPost -> gradingRubric
        else -> null
    }

class SubmitTeamSelfAssessmentUseCase @Inject constructor(
    private val repository: TeamSolutionRepository,
) : SubmitTeamSelfAssessment {

    override suspend fun invoke(taskId: TaskId, draft: SelfAssessmentDraft) =
        repository.submitSelfAssessment(taskId, draft)
}

class DeleteTeamSelfAssessmentUseCase @Inject constructor(
    private val repository: TeamSolutionRepository,
) : DeleteTeamSelfAssessment {

    override suspend fun invoke(taskId: TaskId) = repository.deleteSelfAssessment(taskId)
}

class PreviewTeamSelfAssessmentUseCase @Inject constructor(
    private val repository: TeamSolutionRepository,
) : PreviewTeamSelfAssessment {

    override suspend fun invoke(solutionId: SolutionId, draft: SelfAssessmentDraft) =
        repository.previewTeamGrade(solutionId, draft)
}

class SubmitIndividualSelfAssessmentUseCase @Inject constructor(
    private val repository: SolutionRepository,
) : SubmitIndividualSelfAssessment {

    override suspend fun invoke(taskId: TaskId, draft: SelfAssessmentDraft) =
        repository.submitSelfAssessment(taskId, draft)
}

class DeleteIndividualSelfAssessmentUseCase @Inject constructor(
    private val repository: SolutionRepository,
) : DeleteIndividualSelfAssessment {

    override suspend fun invoke(taskId: TaskId) = repository.deleteSelfAssessment(taskId)
}

class PreviewIndividualSelfAssessmentUseCase @Inject constructor(
    private val repository: SolutionRepository,
) : PreviewIndividualSelfAssessment {

    override suspend fun invoke(solutionId: SolutionId, draft: SelfAssessmentDraft) =
        repository.previewGrade(solutionId, draft)
}
