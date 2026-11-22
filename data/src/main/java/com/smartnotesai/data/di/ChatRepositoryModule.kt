package com.smartnotesai.data.di

import com.smartnotesai.data.repository.ChatRepositoryImpl
import com.smartnotesai.domain.repository.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        implementation: ChatRepositoryImpl
    ): ChatRepository
}
