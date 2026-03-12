package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.PostId
import com.stuf.domain.model.SolutionId

interface AddPostComment {
    suspend operator fun invoke(postId: PostId, text: String): DomainResult<Comment>
}

interface AddSolutionComment {
    suspend operator fun invoke(solutionId: SolutionId, text: String): DomainResult<Comment>
}

interface GetPostComments {
    suspend operator fun invoke(postId: PostId): DomainResult<List<Comment>>
}

interface GetSolutionComments {
    suspend operator fun invoke(solutionId: SolutionId): DomainResult<List<Comment>>
}
