package com.stuf.classroom.di

import com.stuf.domain.usecase.CreatePost
import com.stuf.domain.usecase.DeletePost
import com.stuf.domain.usecase.GetPost
import com.stuf.domain.usecase.UpdatePost
import com.stuf.domain.usecase.impl.CreatePostUseCase
import com.stuf.domain.usecase.impl.DeletePostUseCase
import com.stuf.domain.usecase.impl.GetPostUseCase
import com.stuf.domain.usecase.impl.UpdatePostUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainFeedUseCaseModule {

    @Binds
    @Singleton
    abstract fun bindGetPostUseCase(impl: GetPostUseCase): GetPost

    @Binds
    @Singleton
    abstract fun bindCreatePostUseCase(impl: CreatePostUseCase): CreatePost

    @Binds
    @Singleton
    abstract fun bindUpdatePostUseCase(impl: UpdatePostUseCase): UpdatePost

    @Binds
    @Singleton
    abstract fun bindDeletePostUseCase(impl: DeletePostUseCase): DeletePost
}
