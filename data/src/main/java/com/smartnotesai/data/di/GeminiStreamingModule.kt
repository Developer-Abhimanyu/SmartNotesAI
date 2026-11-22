package com.smartnotesai.data.di

import com.smartnotesai.data.ai.streaming.FirebaseGeminiStreamingClient
import com.smartnotesai.data.ai.streaming.GeminiStreamingClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GeminiStreamingModule {

    @Binds
    @Singleton
    abstract fun bindGeminiStreamingClient(
        implementation: FirebaseGeminiStreamingClient
    ): GeminiStreamingClient
}