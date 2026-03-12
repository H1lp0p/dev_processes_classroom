package com.stuf.domain.usecase.impl

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.PostId
import com.stuf.domain.model.SolutionId
import com.stuf.domain.repository.CommentRepository
import com.stuf.domain.usecase.AddPostComment
import com.stuf.domain.usecase.AddSolutionComment
import com.stuf.domain.usecase.GetPostComments
import com.stuf.domain.usecase.GetSolutionComments

class GetPostCommentsUseCase(
    private val repository: CommentRepository,
) : GetPostComments {

    override suspend fun invoke(postId: PostId): DomainResult<List<Comment>> {
        return repository.getPostComments(postId)
    }
}

class GetSolutionCommentsUseCase(
    private val repository: CommentRepository,
) : GetSolutionComments {

    override suspend fun invoke(solutionId: SolutionId): DomainResult<List<Comment>> {
        return repository.getSolutionComments(solutionId)
    }
}

class AddPostCommentUseCase(
    private val repository: CommentRepository,
) : AddPostComment {

    override suspend fun invoke(postId: PostId, text: String): DomainResult<Comment> {
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            return DomainResult.Failure(DomainError.Validation("Comment text must not be blank"))
        }
        return repository.addPostComment(postId, trimmed)
    }
}

class AddSolutionCommentUseCase(
    private val repository: CommentRepository,
) : AddSolutionComment {

    override suspend fun invoke(solutionId: SolutionId, text: String): DomainResult<Comment> {
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            return DomainResult.Failure(DomainError.Validation("Comment text must not be blank"))
        }
        return repository.addSolutionComment(solutionId, trimmed)
    }
}

