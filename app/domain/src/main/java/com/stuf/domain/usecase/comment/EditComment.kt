package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CommentId

interface EditComment {
    suspend operator fun invoke(commentId: CommentId, text: String): DomainResult<Unit>
}
