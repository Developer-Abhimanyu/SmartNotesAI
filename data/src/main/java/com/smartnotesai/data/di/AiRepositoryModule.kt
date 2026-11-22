package com.smartnotesai.data.di

import com.smartnotesai.data.ai.FirebaseAiRepository
import com.smartnotesai.domain.repository.AiRepository
import com.smartnotesai.domain.repository.MeetingSummaryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAiRepository(firebaseAiRepository: FirebaseAiRepository): AiRepository

    @Binds
    @Singleton
    abstract fun bindMeetingSummaryRepository(
        implementation: FirebaseAiRepository
    ): MeetingSummaryRepository
}