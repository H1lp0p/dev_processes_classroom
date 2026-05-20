package com.stuf.classroom.di

import com.stuf.domain.usecase.DeleteIndividualSelfAssessment
import com.stuf.domain.usecase.DeleteTeamSelfAssessment
import com.stuf.domain.usecase.GetTaskGradingRubric
import com.stuf.domain.usecase.PreviewIndividualSelfAssessment
import com.stuf.domain.usecase.PreviewTeamSelfAssessment
import com.stuf.domain.usecase.SubmitIndividualSelfAssessment
import com.stuf.domain.usecase.SubmitTeamSelfAssessment
import com.stuf.domain.usecase.impl.DeleteIndividualSelfAssessmentUseCase
import com.stuf.domain.usecase.impl.DeleteTeamSelfAssessmentUseCase
import com.stuf.domain.usecase.impl.GetTaskGradingRubricUseCase
import com.stuf.domain.usecase.impl.PreviewIndividualSelfAssessmentUseCase
import com.stuf.domain.usecase.impl.PreviewTeamSelfAssessmentUseCase
import com.stuf.domain.usecase.impl.SubmitIndividualSelfAssessmentUseCase
import com.stuf.domain.usecase.impl.SubmitTeamSelfAssessmentUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainSelfAssessmentUseCaseModule {

    @Binds
    @Singleton
    abstract fun bindGetTaskGradingRubric(impl: GetTaskGradingRubricUseCase): GetTaskGradingRubric

    @Binds
    @Singleton
    abstract fun bindSubmitTeamSelfAssessment(impl: SubmitTeamSelfAssessmentUseCase): SubmitTeamSelfAssessment

    @Binds
    @Singleton
    abstract fun bindDeleteTeamSelfAssessment(impl: DeleteTeamSelfAssessmentUseCase): DeleteTeamSelfAssessment

    @Binds
    @Singleton
    abstract fun bindPreviewTeamSelfAssessment(impl: PreviewTeamSelfAssessmentUseCase): PreviewTeamSelfAssessment

    @Binds
    @Singleton
    abstract fun bindSubmitIndividualSelfAssessment(
        impl: SubmitIndividualSelfAssessmentUseCase,
    ): SubmitIndividualSelfAssessment

    @Binds
    @Singleton
    abstract fun bindDeleteIndividualSelfAssessment(
        impl: DeleteIndividualSelfAssessmentUseCase,
    ): DeleteIndividualSelfAssessment

    @Binds
    @Singleton
    abstract fun bindPreviewIndividualSelfAssessment(
        impl: PreviewIndividualSelfAssessmentUseCase,
    ): PreviewIndividualSelfAssessment
}
