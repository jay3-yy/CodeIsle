package com.openisle.android.di

import com.openisle.android.data.repository.AuthRepositoryImpl
import com.openisle.android.data.repository.PostRepositoryImpl
import com.openisle.android.domain.repository.AuthRepository
import com.openisle.android.domain.repository.PostRepository
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
    abstract fun bindPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): PostRepository

    // ✅ 在此处新增对 AuthRepository 的绑定
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}