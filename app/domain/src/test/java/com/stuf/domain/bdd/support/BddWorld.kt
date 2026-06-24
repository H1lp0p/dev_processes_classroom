package com.stuf.domain.bdd.support

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.GradingMode
import com.stuf.domain.model.PeerReviewId
import com.stuf.domain.model.PeerReviewProgress
import com.stuf.domain.model.PeerReviewTarget
import com.stuf.domain.model.PeerReviewTeamTarget
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.Review
import com.stuf.domain.model.Solution
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.TeamTaskSolution
import com.stuf.domain.repository.PeerReviewRepository
import com.stuf.domain.repository.PostRepository
import com.stuf.domain.repository.SolutionRepository
import com.stuf.domain.repository.TeamSolutionRepository
import com.stuf.domain.usecase.impl.CreatePostUseCase
import com.stuf.domain.usecase.impl.FinishIndividualPeerReviewUseCase
import com.stuf.domain.usecase.impl.GetIndividualPeerReviewProgressUseCase
import com.stuf.domain.usecase.impl.GetAvailableTeamPeerReviewsUseCase
import com.stuf.domain.usecase.impl.GetNextPeerReviewUseCase
import com.stuf.domain.usecase.impl.GetTeamPeerReviewProgressUseCase
import com.stuf.domain.usecase.impl.GetUserSolutionUseCase
import com.stuf.domain.usecase.impl.ReviewSolutionUseCase
import com.stuf.domain.usecase.impl.SubmitPeerReviewUseCase
import com.stuf.domain.usecase.impl.SubmitSolutionUseCase
import com.stuf.domain.usecase.impl.SubmitTeamPeerReviewUseCase
import com.stuf.domain.usecase.impl.UpdatePostUseCase
import com.stuf.grading.domain.model.SelfAssessmentDraft
import com.stuf.domain.model.Score
import com.stuf.domain.model.UserId
import com.stuf.domain.model.GradeBreakdown

/**
 * Точка входа для Cucumber step definitions: in-memory backend + use cases.
 */
object BddWorld {
    val backend = BddBackend()

    lateinit var harness: BddHarness
        private set

    var lastPost: Post? = null
    var lastResult: DomainResult<*>? = null

    fun reset() {
        backend.reset()
        harness = BddHarness(backend)
        lastPost = null
        lastResult = null
    }

    fun capture(result: DomainResult<*>) {
        lastResult = result
        backend.lastError = (result as? DomainResult.Failure)?.error
    }

    fun errorKind(): String? =
        when (backend.lastError) {
            is DomainError.Validation -> "BadRequest"
            is DomainError.Forbidden -> "Forbidden"
            null -> null
            else -> backend.lastError!!::class.simpleName
        }

    fun validationMessage(): String? =
        (backend.lastError as? DomainError.Validation)?.message

    fun isEntryExists(): Boolean =
        validationMessage()?.contains("EntryExists", ignoreCase = true) == true
}

class BddHarness(private val backend: BddBackend) {
    private val postRepo = BddPostRepository(backend)
    private val solutionRepo = BddSolutionRepository(backend)
    private val peerReviewRepo = BddPeerReviewRepository(backend)
    private val teamSolutionRepo = BddTeamSolutionRepository(backend)

    val createPost = CreatePostUseCase(postRepo)
    val updatePost = UpdatePostUseCase(postRepo)
    val submitSolution = SubmitSolutionUseCase(solutionRepo)
    val getUserSolution = GetUserSolutionUseCase(solutionRepo)
    val reviewSolution = ReviewSolutionUseCase(solutionRepo)
    val getNextPeerReview = GetNextPeerReviewUseCase(peerReviewRepo)
    val submitPeerReview = SubmitPeerReviewUseCase(peerReviewRepo)
    val getIndividualPeerReviewProgress = GetIndividualPeerReviewProgressUseCase(peerReviewRepo)
    val finishIndividualPeerReview = FinishIndividualPeerReviewUseCase(peerReviewRepo)
    val getAvailableTeamPeerReviews = GetAvailableTeamPeerReviewsUseCase(peerReviewRepo)
    val submitTeamPeerReview = SubmitTeamPeerReviewUseCase(peerReviewRepo)
    val getTeamPeerReviewProgress = GetTeamPeerReviewProgressUseCase(peerReviewRepo)
}

private class BddPostRepository(private val backend: BddBackend) : PostRepository {
    override suspend fun getCourseFeed(
        courseId: CourseId,
        skip: Int,
        take: Int,
    ): DomainResult<List<Post>> = DomainResult.Success(backend.posts.values.toList())

    override suspend fun getPost(postId: PostId): DomainResult<Post> {
        val post = backend.posts[postId] ?: return DomainResult.Failure(DomainError.NotFound)
        return DomainResult.Success(post)
    }

    override suspend fun createPost(courseId: CourseId, post: Post): DomainResult<Post> =
        backend.createAssignment()

    override suspend fun updatePost(postId: PostId, post: Post): DomainResult<Post> {
        val existing = backend.posts[postId] ?: return DomainResult.Failure(DomainError.NotFound)
        val newMode =
            when (post) {
                is com.stuf.domain.model.TaskPost -> post.gradingMode
                is com.stuf.domain.model.TeamTaskPost -> post.gradingMode
                else -> return DomainResult.Failure(DomainError.Validation("Not a task"))
            }
        return backend.updateGradingMode(newMode)
    }

    override suspend fun deletePost(postId: PostId): DomainResult<Unit> = DomainResult.Success(Unit)
}

private class BddSolutionRepository(private val backend: BddBackend) : SolutionRepository {
    override suspend fun submitSolution(
        taskId: TaskId,
        text: String?,
        fileIds: List<String>,
    ): DomainResult<Solution> {
        val name = backend.currentStudentName ?: "Студент"
        return backend.submitIndividualSolution(name)
    }

    override suspend fun cancelSolution(taskId: TaskId): DomainResult<Unit> =
        DomainResult.Success(Unit)

    override suspend fun getUserSolution(taskId: TaskId): DomainResult<Solution?> {
        val name = backend.currentStudentName ?: "Студент"
        return backend.getUserSolution(name)
    }

    override suspend fun getTaskSolutions(
        taskId: TaskId,
        status: SolutionStatus?,
        studentId: UserId?,
    ): DomainResult<List<Solution>> = DomainResult.Success(emptyList())

    override suspend fun reviewSolution(solutionId: SolutionId, review: Review): DomainResult<Unit> =
        backend.teacherReviewSolution(review.score.value)

    override suspend fun submitSelfAssessment(
        taskId: TaskId,
        draft: SelfAssessmentDraft,
    ): DomainResult<Unit> = DomainResult.Success(Unit)

    override suspend fun deleteSelfAssessment(taskId: TaskId): DomainResult<Unit> =
        DomainResult.Success(Unit)

    override suspend fun previewGrade(
        solutionId: SolutionId,
        draft: SelfAssessmentDraft,
    ): DomainResult<GradeBreakdown> = DomainResult.Failure(DomainError.Unknown())
}

private class BddPeerReviewRepository(private val backend: BddBackend) : PeerReviewRepository {
    override suspend fun getNextPeerReview(taskId: TaskId): DomainResult<PeerReviewTarget?> {
        val name = backend.currentStudentName ?: return DomainResult.Failure(DomainError.Unauthorized)
        return backend.getNextPeerReview(name)
    }

    override suspend fun submitPeerReview(
        reviewId: PeerReviewId,
        draft: SelfAssessmentDraft,
    ): DomainResult<PeerReviewProgress> {
        val name = backend.currentStudentName ?: return DomainResult.Failure(DomainError.Unauthorized)
        return backend.submitPeerReview(name, reviewId, draft)
    }

    override suspend fun getIndividualPeerReviewProgress(taskId: TaskId): DomainResult<PeerReviewProgress> {
        val name = backend.currentStudentName ?: return DomainResult.Failure(DomainError.Unauthorized)
        return backend.getIndividualProgress(name)
    }

    override suspend fun finishIndividualPeerReview(taskId: TaskId): DomainResult<PeerReviewProgress> {
        val name = backend.currentStudentName ?: return DomainResult.Failure(DomainError.Unauthorized)
        return backend.finishPeerReview(name)
    }

    override suspend fun getAvailableTeamPeerReviews(taskId: TaskId): DomainResult<List<PeerReviewTeamTarget>> {
        val name = backend.currentStudentName ?: return DomainResult.Failure(DomainError.Unauthorized)
        return backend.getAvailableTeamReviews(name)
    }

    override suspend fun submitTeamPeerReview(
        teamSolutionId: SolutionId,
        draft: SelfAssessmentDraft,
    ): DomainResult<PeerReviewProgress> {
        val name = backend.currentStudentName ?: return DomainResult.Failure(DomainError.Unauthorized)
        val teamName =
            backend.lastAvailableTeamTargets
                .firstOrNull { it.teamSolutionId == teamSolutionId }
                ?.teamName
                ?: backend.teams.entries.firstOrNull { (_, teamId) ->
                    backend.teamSolutions.values.any { it.id == teamSolutionId && it.team.id == teamId }
                }?.key
                ?: return DomainResult.Failure(DomainError.NotFound)
        return backend.submitTeamPeerReview(name, teamName, draft)
    }

    override suspend fun getTeamPeerReviewProgress(taskId: TaskId): DomainResult<PeerReviewProgress> {
        val name = backend.currentStudentName ?: return DomainResult.Failure(DomainError.Unauthorized)
        return backend.getTeamProgress(name)
    }
}

private class BddTeamSolutionRepository(private val backend: BddBackend) : TeamSolutionRepository {
    override suspend fun getTeamSolution(taskId: TaskId): DomainResult<TeamTaskSolution?> {
        val name = backend.currentStudentName ?: "Студент"
        return backend.getTeamSolutionForStudent(name)
    }

    override suspend fun submitTeamSolution(
        taskId: TaskId,
        text: String?,
        fileIds: List<String>,
        selfAssessment: SelfAssessmentDraft?,
    ): DomainResult<SolutionId> {
        val teamName =
            backend.userTeam[backend.students[backend.currentStudentName]]?.let { id ->
                backend.teams.entries.firstOrNull { it.value == id }?.key
            } ?: "A"
        val result = backend.submitTeamSolution(teamName)
        return when (result) {
            is DomainResult.Success -> DomainResult.Success(result.value.id ?: SolutionId(java.util.UUID.randomUUID()))
            is DomainResult.Failure -> result
        }
    }

    override suspend fun deleteTeamSolution(taskId: TaskId): DomainResult<Unit> =
        DomainResult.Success(Unit)

    override suspend fun submitSelfAssessment(
        taskId: TaskId,
        draft: SelfAssessmentDraft,
    ): DomainResult<Unit> = DomainResult.Success(Unit)

    override suspend fun deleteSelfAssessment(taskId: TaskId): DomainResult<Unit> =
        DomainResult.Success(Unit)

    override suspend fun previewTeamGrade(
        solutionId: SolutionId,
        draft: SelfAssessmentDraft,
    ): DomainResult<GradeBreakdown> = DomainResult.Failure(DomainError.Unknown())
}
