package com.stuf.classroom.di

import com.stuf.domain.usecase.CancelTeamTaskSolution
import com.stuf.domain.usecase.CheckTeamCaptain
import com.stuf.domain.usecase.GetGradeDistribution
import com.stuf.domain.usecase.GetMyTeamForTeamTask
import com.stuf.domain.usecase.GetTeamTaskSolution
import com.stuf.domain.usecase.GetTeamsForTeamTask
import com.stuf.domain.usecase.JoinTeam
import com.stuf.domain.usecase.LeaveTeam
import com.stuf.domain.usecase.SaveGradeDistribution
import com.stuf.domain.usecase.SubmitTeamTaskSolution
import com.stuf.domain.usecase.TransferTeamCaptain
import com.stuf.domain.usecase.VoteOnGradeDistribution
import com.stuf.domain.usecase.VoteTeamCaptain
import com.stuf.domain.usecase.impl.CancelTeamTaskSolutionUseCase
import com.stuf.domain.usecase.impl.CheckTeamCaptainUseCase
import com.stuf.domain.usecase.impl.GetGradeDistributionUseCase
import com.stuf.domain.usecase.impl.GetMyTeamForTeamTaskUseCase
import com.stuf.domain.usecase.impl.GetTeamTaskSolutionUseCase
import com.stuf.domain.usecase.impl.GetTeamsForTeamTaskUseCase
import com.stuf.domain.usecase.impl.JoinTeamUseCase
import com.stuf.domain.usecase.impl.LeaveTeamUseCase
import com.stuf.domain.usecase.impl.SaveGradeDistributionUseCase
import com.stuf.domain.usecase.impl.SubmitTeamTaskSolutionUseCase
import com.stuf.domain.usecase.impl.TransferTeamCaptainUseCase
import com.stuf.domain.usecase.impl.VoteOnGradeDistributionUseCase
import com.stuf.domain.usecase.impl.VoteTeamCaptainUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainTeamUseCaseModule {

    @Binds
    @Singleton
    abstract fun bindGetTeamsForTeamTask(impl: GetTeamsForTeamTaskUseCase): GetTeamsForTeamTask

    @Binds
    @Singleton
    abstract fun bindGetMyTeamForTeamTask(impl: GetMyTeamForTeamTaskUseCase): GetMyTeamForTeamTask

    @Binds
    @Singleton
    abstract fun bindJoinTeam(impl: JoinTeamUseCase): JoinTeam

    @Binds
    @Singleton
    abstract fun bindLeaveTeam(impl: LeaveTeamUseCase): LeaveTeam

    @Binds
    @Singleton
    abstract fun bindTransferTeamCaptain(impl: TransferTeamCaptainUseCase): TransferTeamCaptain

    @Binds
    @Singleton
    abstract fun bindVoteTeamCaptain(impl: VoteTeamCaptainUseCase): VoteTeamCaptain

    @Binds
    @Singleton
    abstract fun bindCheckTeamCaptain(impl: CheckTeamCaptainUseCase): CheckTeamCaptain

    @Binds
    @Singleton
    abstract fun bindGetTeamTaskSolution(impl: GetTeamTaskSolutionUseCase): GetTeamTaskSolution

    @Binds
    @Singleton
    abstract fun bindSubmitTeamTaskSolution(impl: SubmitTeamTaskSolutionUseCase): SubmitTeamTaskSolution

    @Binds
    @Singleton
    abstract fun bindCancelTeamTaskSolution(impl: CancelTeamTaskSolutionUseCase): CancelTeamTaskSolution

    @Binds
    @Singleton
    abstract fun bindGetGradeDistribution(impl: GetGradeDistributionUseCase): GetGradeDistribution

    @Binds
    @Singleton
    abstract fun bindSaveGradeDistribution(impl: SaveGradeDistributionUseCase): SaveGradeDistribution

    @Binds
    @Singleton
    abstract fun bindVoteOnGradeDistribution(impl: VoteOnGradeDistributionUseCase): VoteOnGradeDistribution
}
