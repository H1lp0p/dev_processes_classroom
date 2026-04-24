package com.stuf.data.di

import com.stuf.data.demo.DemoAuthRepository
import com.stuf.data.demo.DemoCommentRepository
import com.stuf.data.demo.DemoCurrentUserRepository
import com.stuf.data.demo.DemoCourseRepository
import com.stuf.data.demo.DemoFileRepository
import com.stuf.data.demo.DemoGradeDistributionRepository
import com.stuf.data.demo.DemoPerformanceRepository
import com.stuf.data.demo.DemoPostRepository
import com.stuf.data.demo.DemoSolutionRepository
import com.stuf.data.demo.DemoTeamRepository
import com.stuf.data.demo.DemoTeamSolutionRepository
import com.stuf.domain.repository.AuthRepository
import com.stuf.domain.repository.CommentRepository
import com.stuf.domain.repository.CurrentUserRepository
import com.stuf.domain.repository.CourseRepository
import com.stuf.domain.repository.FileRepository
import com.stuf.domain.repository.GradeDistributionRepository
import com.stuf.domain.repository.PerformanceRepository
import com.stuf.domain.repository.PostRepository
import com.stuf.domain.repository.SolutionRepository
import com.stuf.domain.repository.TeamRepository
import com.stuf.domain.repository.TeamSolutionRepository
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
        impl: DemoAuthRepository,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCourseRepository(
        impl: DemoCourseRepository,
    ): CourseRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        impl: DemoPostRepository,
    ): PostRepository

    @Binds
    @Singleton
    abstract fun bindSolutionRepository(
        impl: DemoSolutionRepository,
    ): SolutionRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(
        impl: DemoCommentRepository,
    ): CommentRepository

    @Binds
    @Singleton
    abstract fun bindFileRepository(
        impl: DemoFileRepository,
    ): FileRepository

    @Binds
    @Singleton
    abstract fun bindPerformanceRepository(
        impl: DemoPerformanceRepository,
    ): PerformanceRepository

    @Binds
    @Singleton
    abstract fun bindCurrentUserRepository(
        impl: DemoCurrentUserRepository,
    ): CurrentUserRepository

    @Binds
    @Singleton
    abstract fun bindTeamRepository(
        impl: DemoTeamRepository,
    ): TeamRepository

    @Binds
    @Singleton
    abstract fun bindTeamSolutionRepository(
        impl: DemoTeamSolutionRepository,
    ): TeamSolutionRepository

    @Binds
    @Singleton
    abstract fun bindGradeDistributionRepository(
        impl: DemoGradeDistributionRepository,
    ): GradeDistributionRepository
}
