package com.stuf.data.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.stuf.data.auth.AuthSessionStorage
import com.stuf.data.auth.AuthSessionStorageImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore(name = "auth_session")

@Module
@InstallIn(SingletonComponent::class)
object AuthStorageModule {

    @Provides
    @Singleton
    fun provideAuthSessionStorage(
        @ApplicationContext context: Context,
    ): AuthSessionStorage {
        val dataStore = context.authDataStore
        return AuthSessionStorageImpl(dataStore)
    }
}

