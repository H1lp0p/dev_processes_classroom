package com.stuf.data.di

import com.stuf.data.repository.AuthRepositoryImpl
import com.stuf.data.repository.CommentRepositoryImpl
import com.stuf.data.repository.CourseRepositoryImpl
import com.stuf.data.repository.CurrentUserRepositoryImpl
import com.stuf.data.repository.PostRepositoryImpl
import com.stuf.data.repository.SolutionRepositoryImpl
import com.stuf.data.stub.UnimplementedFileRepository
import com.stuf.data.stub.UnimplementedPerformanceRepository
import com.stuf.domain.repository.AuthRepository
import com.stuf.domain.repository.CommentRepository
import com.stuf.domain.repository.CurrentUserRepository
import com.stuf.domain.repository.CourseRepository
import com.stuf.domain.repository.FileRepository
import com.stuf.domain.repository.PerformanceRepository
import com.stuf.domain.repository.PostRepository
import com.stuf.domain.repository.SolutionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCourseRepository(
        impl: CourseRepositoryImpl,
    ): CourseRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        impl: PostRepositoryImpl,
    ): PostRepository

    @Binds
    @Singleton
    abstract fun bindSolutionRepository(
        impl: SolutionRepositoryImpl,
    ): SolutionRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(
        impl: CommentRepositoryImpl,
    ): CommentRepository

    @Binds
    @Singleton
    abstract fun bindFileRepository(
        impl: UnimplementedFileRepository,
    ): FileRepository

    @Binds
    @Singleton
    abstract fun bindPerformanceRepository(
        impl: UnimplementedPerformanceRepository,
    ): PerformanceRepository

    @Binds
    @Singleton
    abstract fun bindCurrentUserRepository(
        impl: CurrentUserRepositoryImpl,
    ): CurrentUserRepository
}
