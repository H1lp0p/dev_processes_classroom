package com.stuf.domain.repository

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.CommentId
import com.stuf.domain.model.PostId
import com.stuf.domain.model.SolutionId

interface CommentRepository {
    suspend fun getPostComments(postId: PostId): DomainResult<List<Comment>>
    suspend fun getSolutionComments(solutionId: SolutionId): DomainResult<List<Comment>>
    suspend fun addPostComment(postId: PostId, text: String): DomainResult<Comment>
    suspend fun addSolutionComment(solutionId: SolutionId, text: String): DomainResult<Comment>
    suspend fun getCommentReplies(commentId: CommentId): DomainResult<List<Comment>>
    suspend fun addCommentReply(commentId: CommentId, text: String): DomainResult<Comment>
    suspend fun editComment(commentId: CommentId, text: String): DomainResult<Unit>
    suspend fun deleteComment(commentId: CommentId): DomainResult<Unit>
}
