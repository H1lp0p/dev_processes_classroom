package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CommentId

interface DeleteComment {
    suspend operator fun invoke(commentId: CommentId): DomainResult<Unit>
}
