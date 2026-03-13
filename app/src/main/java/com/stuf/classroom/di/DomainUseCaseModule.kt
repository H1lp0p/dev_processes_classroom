package com.stuf.classroom.di

import com.stuf.domain.usecase.AddPostComment
import com.stuf.domain.usecase.AddSolutionComment
import com.stuf.domain.usecase.CancelSolution
import com.stuf.domain.usecase.ChangeMemberRole
import com.stuf.domain.usecase.CreateCourse
import com.stuf.domain.usecase.CreatePost
import com.stuf.domain.usecase.DeletePost
import com.stuf.domain.usecase.GetCourseFeed
import com.stuf.domain.usecase.GetCourseMembers
import com.stuf.domain.usecase.GetPerformanceTable
import com.stuf.domain.usecase.GetPost
import com.stuf.domain.usecase.GetPostComments
import com.stuf.domain.usecase.GetSolutionComments
import com.stuf.domain.usecase.GetSolutionsForTask
import com.stuf.domain.usecase.GetUserSolution
import com.stuf.domain.usecase.JoinCourse
import com.stuf.domain.usecase.LeaveCourse
import com.stuf.domain.usecase.RemoveMember
import com.stuf.domain.usecase.ReviewSolution
import com.stuf.domain.usecase.SubmitSolution
import com.stuf.domain.usecase.UpdatePost
import com.stuf.domain.usecase.UpdateSolution
import com.stuf.domain.usecase.impl.AddPostCommentUseCase
import com.stuf.domain.usecase.impl.AddSolutionCommentUseCase
import com.stuf.domain.usecase.impl.CancelSolutionUseCase
import com.stuf.domain.usecase.impl.ChangeMemberRoleUseCase
import com.stuf.domain.usecase.impl.CreateCourseUseCase
import com.stuf.domain.usecase.impl.CreatePostUseCase
import com.stuf.domain.usecase.impl.DeletePostUseCase
import com.stuf.domain.usecase.impl.GetCourseFeedUseCase
import com.stuf.domain.usecase.impl.GetCourseMembersUseCase
import com.stuf.domain.usecase.impl.GetPerformanceTableUseCase
import com.stuf.domain.usecase.impl.GetPostCommentsUseCase
import com.stuf.domain.usecase.impl.GetPostUseCase
import com.stuf.domain.usecase.impl.GetSolutionCommentsUseCase
import com.stuf.domain.usecase.impl.GetSolutionsForTaskUseCase
import com.stuf.domain.usecase.impl.GetUserSolutionUseCase
import com.stuf.domain.usecase.impl.JoinCourseUseCase
import com.stuf.domain.usecase.impl.LeaveCourseUseCase
import com.stuf.domain.usecase.impl.RemoveMemberUseCase
import com.stuf.domain.usecase.impl.ReviewSolutionUseCase
import com.stuf.domain.usecase.impl.SubmitSolutionUseCase
import com.stuf.domain.usecase.impl.UpdatePostUseCase
import com.stuf.domain.usecase.impl.UpdateSolutionUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainUseCaseModule {

    @Binds
    @Singleton
    abstract fun bindCreateCourseUseCase(
        impl: CreateCourseUseCase,
    ): CreateCourse

    @Binds
    @Singleton
    abstract fun bindJoinCourseUseCase(
        impl: JoinCourseUseCase,
    ): JoinCourse

    @Binds
    @Singleton
    abstract fun bindGetCourseMembersUseCase(
        impl: GetCourseMembersUseCase,
    ): GetCourseMembers

    @Binds
    @Singleton
    abstract fun bindChangeMemberRoleUseCase(
        impl: ChangeMemberRoleUseCase,
    ): ChangeMemberRole

    @Binds
    @Singleton
    abstract fun bindRemoveMemberUseCase(
        impl: RemoveMemberUseCase,
    ): RemoveMember

    @Binds
    @Singleton
    abstract fun bindLeaveCourseUseCase(
        impl: LeaveCourseUseCase,
    ): LeaveCourse

    @Binds
    @Singleton
    abstract fun bindGetCourseFeedUseCase(
        impl: GetCourseFeedUseCase,
    ): GetCourseFeed

    @Binds
    @Singleton
    abstract fun bindGetPostUseCase(
        impl: GetPostUseCase,
    ): GetPost

    @Binds
    @Singleton
    abstract fun bindCreatePostUseCase(
        impl: CreatePostUseCase,
    ): CreatePost

    @Binds
    @Singleton
    abstract fun bindUpdatePostUseCase(
        impl: UpdatePostUseCase,
    ): UpdatePost

    @Binds
    @Singleton
    abstract fun bindDeletePostUseCase(
        impl: DeletePostUseCase,
    ): DeletePost

    @Binds
    @Singleton
    abstract fun bindSubmitSolutionUseCase(
        impl: SubmitSolutionUseCase,
    ): SubmitSolution

    @Binds
    @Singleton
    abstract fun bindUpdateSolutionUseCase(
        impl: UpdateSolutionUseCase,
    ): UpdateSolution

    @Binds
    @Singleton
    abstract fun bindCancelSolutionUseCase(
        impl: CancelSolutionUseCase,
    ): CancelSolution

    @Binds
    @Singleton
    abstract fun bindGetSolutionsForTaskUseCase(
        impl: GetSolutionsForTaskUseCase,
    ): GetSolutionsForTask

    @Binds
    @Singleton
    abstract fun bindReviewSolutionUseCase(
        impl: ReviewSolutionUseCase,
    ): ReviewSolution

    @Binds
    @Singleton
    abstract fun bindGetUserSolutionUseCase(
        impl: GetUserSolutionUseCase,
    ): GetUserSolution

    @Binds
    @Singleton
    abstract fun bindGetPostCommentsUseCase(
        impl: GetPostCommentsUseCase,
    ): GetPostComments

    @Binds
    @Singleton
    abstract fun bindGetSolutionCommentsUseCase(
        impl: GetSolutionCommentsUseCase,
    ): GetSolutionComments

    @Binds
    @Singleton
    abstract fun bindAddPostCommentUseCase(
        impl: AddPostCommentUseCase,
    ): AddPostComment

    @Binds
    @Singleton
    abstract fun bindAddSolutionCommentUseCase(
        impl: AddSolutionCommentUseCase,
    ): AddSolutionComment

    @Binds
    @Singleton
    abstract fun bindGetPerformanceTableUseCase(
        impl: GetPerformanceTableUseCase,
    ): GetPerformanceTable
}

