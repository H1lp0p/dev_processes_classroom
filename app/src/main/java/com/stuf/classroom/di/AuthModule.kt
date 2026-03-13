package com.stuf.classroom.di

import com.stuf.classroom.auth.AuthManager
import com.stuf.classroom.auth.DefaultAuthManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthManager(
        impl: DefaultAuthManager,
    ): AuthManager
}

