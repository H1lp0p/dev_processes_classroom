package com.stuf.classroom.di

import com.stuf.domain.usecase.ChangeMemberRole
import com.stuf.domain.usecase.CreateCourse
import com.stuf.domain.usecase.GetCourseFeed
import com.stuf.domain.usecase.GetCourseInfo
import com.stuf.domain.usecase.GetCourseMembers
import com.stuf.domain.usecase.GetUserCourses
import com.stuf.domain.usecase.JoinCourse
import com.stuf.domain.usecase.LeaveCourse
import com.stuf.domain.usecase.RemoveMember
import com.stuf.domain.usecase.impl.ChangeMemberRoleUseCase
import com.stuf.domain.usecase.impl.CreateCourseUseCase
import com.stuf.domain.usecase.impl.GetCourseFeedUseCase
import com.stuf.domain.usecase.impl.GetCourseInfoUseCase
import com.stuf.domain.usecase.impl.GetCourseMembersUseCase
import com.stuf.domain.usecase.impl.GetUserCoursesUseCase
import com.stuf.domain.usecase.impl.JoinCourseUseCase
import com.stuf.domain.usecase.impl.LeaveCourseUseCase
import com.stuf.domain.usecase.impl.RemoveMemberUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainCourseUseCaseModule {

    @Binds
    @Singleton
    abstract fun bindCreateCourseUseCase(impl: CreateCourseUseCase): CreateCourse

    @Binds
    @Singleton
    abstract fun bindJoinCourseUseCase(impl: JoinCourseUseCase): JoinCourse

    @Binds
    @Singleton
    abstract fun bindGetCourseMembersUseCase(impl: GetCourseMembersUseCase): GetCourseMembers

    @Binds
    @Singleton
    abstract fun bindGetCourseInfoUseCase(impl: GetCourseInfoUseCase): GetCourseInfo

    @Binds
    @Singleton
    abstract fun bindChangeMemberRoleUseCase(impl: ChangeMemberRoleUseCase): ChangeMemberRole

    @Binds
    @Singleton
    abstract fun bindRemoveMemberUseCase(impl: RemoveMemberUseCase): RemoveMember

    @Binds
    @Singleton
    abstract fun bindLeaveCourseUseCase(impl: LeaveCourseUseCase): LeaveCourse

    @Binds
    @Singleton
    abstract fun bindGetUserCoursesUseCase(impl: GetUserCoursesUseCase): GetUserCourses

    @Binds
    @Singleton
    abstract fun bindGetCourseFeedUseCase(impl: GetCourseFeedUseCase): GetCourseFeed
}
