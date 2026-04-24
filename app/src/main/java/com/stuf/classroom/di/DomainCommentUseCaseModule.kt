package com.stuf.classroom.di

import com.stuf.domain.usecase.AddCommentReply
import com.stuf.domain.usecase.AddPostComment
import com.stuf.domain.usecase.AddSolutionComment
import com.stuf.domain.usecase.DeleteComment
import com.stuf.domain.usecase.EditComment
import com.stuf.domain.usecase.GetCommentReplies
import com.stuf.domain.usecase.GetPostComments
import com.stuf.domain.usecase.GetSolutionComments
import com.stuf.domain.usecase.impl.AddCommentReplyUseCase
import com.stuf.domain.usecase.impl.AddPostCommentUseCase
import com.stuf.domain.usecase.impl.AddSolutionCommentUseCase
import com.stuf.domain.usecase.impl.DeleteCommentUseCase
import com.stuf.domain.usecase.impl.EditCommentUseCase
import com.stuf.domain.usecase.impl.GetCommentRepliesUseCase
import com.stuf.domain.usecase.impl.GetPostCommentsUseCase
import com.stuf.domain.usecase.impl.GetSolutionCommentsUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainCommentUseCaseModule {

    @Binds
    @Singleton
    abstract fun bindGetPostCommentsUseCase(impl: GetPostCommentsUseCase): GetPostComments

    @Binds
    @Singleton
    abstract fun bindGetSolutionCommentsUseCase(impl: GetSolutionCommentsUseCase): GetSolutionComments

    @Binds
    @Singleton
    abstract fun bindAddPostCommentUseCase(impl: AddPostCommentUseCase): AddPostComment

    @Binds
    @Singleton
    abstract fun bindAddSolutionCommentUseCase(impl: AddSolutionCommentUseCase): AddSolutionComment

    @Binds
    @Singleton
    abstract fun bindAddCommentReplyUseCase(impl: AddCommentReplyUseCase): AddCommentReply

    @Binds
    @Singleton
    abstract fun bindGetCommentRepliesUseCase(impl: GetCommentRepliesUseCase): GetCommentReplies

    @Binds
    @Singleton
    abstract fun bindEditCommentUseCase(impl: EditCommentUseCase): EditComment

    @Binds
    @Singleton
    abstract fun bindDeleteCommentUseCase(impl: DeleteCommentUseCase): DeleteComment
}
