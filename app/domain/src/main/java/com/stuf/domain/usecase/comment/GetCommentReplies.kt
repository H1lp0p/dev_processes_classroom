package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.CommentId

interface GetCommentReplies {
    suspend operator fun invoke(commentId: CommentId): DomainResult<List<Comment>>
}
