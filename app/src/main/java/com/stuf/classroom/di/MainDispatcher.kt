package com.stuf.classroom.di

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher
