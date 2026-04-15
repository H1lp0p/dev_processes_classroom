package com.stuf.data.di

import android.util.Log
import com.stuf.data.BuildConfig
import com.stuf.data.api.AuthApi
import com.stuf.data.api.CommentApi
import com.stuf.data.api.CourseApi
import com.stuf.data.api.FilesApi
import com.stuf.data.api.GradeDistributionApi
import com.stuf.data.api.PostApi
import com.stuf.data.api.SolutionApi
import com.stuf.data.api.TeamApi
import com.stuf.data.api.TeamSolutionApi
import com.stuf.data.api.UserApi
import com.stuf.data.auth.BearerTokenApplier
import com.stuf.data.auth.AuthTokenManager
import com.stuf.data.infrastructure.ApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @ApiBaseUrl
    fun provideBaseUrl(): String = BuildConfig.API_BASE_URL

    @Provides
    @Singleton
    fun provideApiClient(
        @ApiBaseUrl baseUrl: String,
    ): ApiClient =
        ApiClient(
            baseUrl = baseUrl,
            authNames = arrayOf("bearerAuth"),
        ).setLogger { message ->
            Log.d("ApiHttp", message)
        }

    @Provides
    @Singleton
    fun provideBearerTokenApplier(
        apiClient: ApiClient,
    ): BearerTokenApplier = BearerTokenApplier { token ->
        apiClient.setBearerToken(token)
    }

    @Provides
    @Singleton
    fun provideAuthTokenManager(
        bearerTokenApplier: BearerTokenApplier,
    ): AuthTokenManager = AuthTokenManager(bearerTokenApplier)

    @Provides
    @Singleton
    fun provideAuthApi(
        apiClient: ApiClient,
    ): AuthApi = apiClient.createService(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideCourseApi(
        apiClient: ApiClient,
    ): CourseApi = apiClient.createService(CourseApi::class.java)

    @Provides
    @Singleton
    fun providePostApi(
        apiClient: ApiClient,
    ): PostApi = apiClient.createService(PostApi::class.java)

    @Provides
    @Singleton
    fun provideCommentApi(
        apiClient: ApiClient,
    ): CommentApi = apiClient.createService(CommentApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(
        apiClient: ApiClient,
    ): UserApi = apiClient.createService(UserApi::class.java)

    @Provides
    @Singleton
    fun provideFilesApi(
        apiClient: ApiClient,
    ): FilesApi = apiClient.createService(FilesApi::class.java)

    @Provides
    @Singleton
    fun provideSolutionApi(
        apiClient: ApiClient,
    ): SolutionApi = apiClient.createService(SolutionApi::class.java)

    @Provides
    @Singleton
    fun provideTeamApi(
        apiClient: ApiClient,
    ): TeamApi = apiClient.createService(TeamApi::class.java)

    @Provides
    @Singleton
    fun provideTeamSolutionApi(
        apiClient: ApiClient,
    ): TeamSolutionApi = apiClient.createService(TeamSolutionApi::class.java)

    @Provides
    @Singleton
    fun provideGradeDistributionApi(
        apiClient: ApiClient,
    ): GradeDistributionApi = apiClient.createService(GradeDistributionApi::class.java)
}

