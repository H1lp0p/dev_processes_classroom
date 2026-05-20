package com.stuf.classroom.di

import com.stuf.domain.usecase.GetPerformanceTable
import com.stuf.domain.usecase.impl.GetPerformanceTableUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainPerformanceUseCaseModule {

    @Binds
    @Singleton
    abstract fun bindGetPerformanceTableUseCase(impl: GetPerformanceTableUseCase): GetPerformanceTable
}
