package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.PostId

interface GetPostComments {
    suspend operator fun invoke(postId: PostId): DomainResult<List<Comment>>
}
