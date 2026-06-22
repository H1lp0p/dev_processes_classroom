package com.stuf.classroom.di

import com.stuf.domain.usecase.FinishIndividualPeerReview
import com.stuf.domain.usecase.GetAvailableTeamPeerReviews
import com.stuf.domain.usecase.GetIndividualPeerReviewProgress
import com.stuf.domain.usecase.GetNextPeerReview
import com.stuf.domain.usecase.GetTeamPeerReviewProgress
import com.stuf.domain.usecase.SubmitPeerReview
import com.stuf.domain.usecase.SubmitTeamPeerReview
import com.stuf.domain.usecase.impl.FinishIndividualPeerReviewUseCase
import com.stuf.domain.usecase.impl.GetAvailableTeamPeerReviewsUseCase
import com.stuf.domain.usecase.impl.GetIndividualPeerReviewProgressUseCase
import com.stuf.domain.usecase.impl.GetNextPeerReviewUseCase
import com.stuf.domain.usecase.impl.GetTeamPeerReviewProgressUseCase
import com.stuf.domain.usecase.impl.SubmitPeerReviewUseCase
import com.stuf.domain.usecase.impl.SubmitTeamPeerReviewUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainPeerReviewUseCaseModule {

    @Binds
    @Singleton
    abstract fun bindGetNextPeerReview(impl: GetNextPeerReviewUseCase): GetNextPeerReview

    @Binds
    @Singleton
    abstract fun bindSubmitPeerReview(impl: SubmitPeerReviewUseCase): SubmitPeerReview

    @Binds
    @Singleton
    abstract fun bindGetIndividualPeerReviewProgress(
        impl: GetIndividualPeerReviewProgressUseCase,
    ): GetIndividualPeerReviewProgress

    @Binds
    @Singleton
    abstract fun bindFinishIndividualPeerReview(
        impl: FinishIndividualPeerReviewUseCase,
    ): FinishIndividualPeerReview

    @Binds
    @Singleton
    abstract fun bindGetAvailableTeamPeerReviews(
        impl: GetAvailableTeamPeerReviewsUseCase,
    ): GetAvailableTeamPeerReviews

    @Binds
    @Singleton
    abstract fun bindSubmitTeamPeerReview(impl: SubmitTeamPeerReviewUseCase): SubmitTeamPeerReview

    @Binds
    @Singleton
    abstract fun bindGetTeamPeerReviewProgress(
        impl: GetTeamPeerReviewProgressUseCase,
    ): GetTeamPeerReviewProgress
}
