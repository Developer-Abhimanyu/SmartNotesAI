package com.smartnotesai.data.ai.streaming

import com.smartnotesai.domain.model.ChatMessage
import com.smartnotesai.domain.model.ImageInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeGeminiStreamingClient(
    var chunks: List<String> = emptyList(),
    var exception: Exception? = null,
    var chatChunks: List<String> = emptyList(),
    var imageChunks: List<String> = emptyList()
) : GeminiStreamingClient {

    var receivedImage: ImageInput? = null
    var receivedPrompt: String? = null

    override fun generateContentStream(
        prompt: String
    ): Flow<String> = flow {
        chunks.forEach { chunk ->
            emit(chunk)
        }

        exception?.let { throw it }
    }

    override fun generateChatResponseStream(
        history: List<ChatMessage>,
        prompt: String
    ): Flow<String> = flow {
        chatChunks.forEach { chunk ->
            emit(chunk)
        }

        exception?.let { throw it }
    }

    override fun generateImageUnderstandingStream(
        image: ImageInput,
        prompt: String
    ): Flow<String> = flow {
        receivedImage = image
        receivedPrompt = prompt

        imageChunks.forEach { chunk ->
            emit(chunk)
        }

        exception?.let { throw it }
    }
}
