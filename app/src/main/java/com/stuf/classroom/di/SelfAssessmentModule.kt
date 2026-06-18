package com.stuf.classroom.di

import com.stuf.classroom.selfassessment.SelfAssessmentFeatureConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SelfAssessmentModule {

    @Provides
    @Singleton
    fun provideSelfAssessmentFeatureConfig(): SelfAssessmentFeatureConfig =
        SelfAssessmentFeatureConfig(enableTeamReflexyScore = true)
}
