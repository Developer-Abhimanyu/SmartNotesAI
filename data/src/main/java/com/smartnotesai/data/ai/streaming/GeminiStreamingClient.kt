package com.smartnotesai.data.ai.streaming

import com.smartnotesai.domain.model.ChatMessage
import com.smartnotesai.domain.model.ImageInput
import kotlinx.coroutines.flow.Flow

interface GeminiStreamingClient {

    fun generateContentStream(
        prompt: String
    ): Flow<String>

    fun generateChatResponseStream(
        history: List<ChatMessage>,
        prompt: String
    ): Flow<String>

    fun generateImageUnderstandingStream(
        image: ImageInput,
        prompt: String
    ): Flow<String>
}
