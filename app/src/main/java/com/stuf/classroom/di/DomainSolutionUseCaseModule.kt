package com.stuf.classroom.di

import com.stuf.domain.usecase.CancelSolution
import com.stuf.domain.usecase.GetSolutionsForTask
import com.stuf.domain.usecase.GetUserSolution
import com.stuf.domain.usecase.ReviewSolution
import com.stuf.domain.usecase.SubmitSolution
import com.stuf.domain.usecase.UpdateSolution
import com.stuf.domain.usecase.impl.CancelSolutionUseCase
import com.stuf.domain.usecase.impl.GetSolutionsForTaskUseCase
import com.stuf.domain.usecase.impl.GetUserSolutionUseCase
import com.stuf.domain.usecase.impl.ReviewSolutionUseCase
import com.stuf.domain.usecase.impl.SubmitSolutionUseCase
import com.stuf.domain.usecase.impl.UpdateSolutionUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainSolutionUseCaseModule {

    @Binds
    @Singleton
    abstract fun bindSubmitSolutionUseCase(impl: SubmitSolutionUseCase): SubmitSolution

    @Binds
    @Singleton
    abstract fun bindUpdateSolutionUseCase(impl: UpdateSolutionUseCase): UpdateSolution

    @Binds
    @Singleton
    abstract fun bindCancelSolutionUseCase(impl: CancelSolutionUseCase): CancelSolution

    @Binds
    @Singleton
    abstract fun bindGetSolutionsForTaskUseCase(impl: GetSolutionsForTaskUseCase): GetSolutionsForTask

    @Binds
    @Singleton
    abstract fun bindReviewSolutionUseCase(impl: ReviewSolutionUseCase): ReviewSolution

    @Binds
    @Singleton
    abstract fun bindGetUserSolutionUseCase(impl: GetUserSolutionUseCase): GetUserSolution
}
