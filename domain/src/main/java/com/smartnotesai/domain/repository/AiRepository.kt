package com.smartnotesai.domain.repository

import com.smartnotesai.domain.model.ChatMessage
import com.smartnotesai.domain.model.ImageInput
import kotlinx.coroutines.flow.Flow

interface AiRepository {

    suspend fun generateText(prompt: String): String

    fun generateTextStream(prompt: String): Flow<String>

    /**
     * Generates a streaming response using previous conversation messages
     * as context.
     *
     * @param history Previous messages in chronological order.
     * @param prompt The latest user message.
     */
    fun generateChatResponseStream(
        history: List<ChatMessage>,
        prompt: String
    ): Flow<String> {
        // Temporary backward-compatible implementation.
        // The Firebase repository overrides this method.
        return generateTextStream(prompt)
    }

    /**
     * Generates a streaming response based on an image and text prompt.
     *
     * @param image The image data and MIME type.
     * @param prompt The user's question about the image.
     */
    fun generateImageUnderstandingStream(
        image: ImageInput,
        prompt: String
    ): Flow<String>
}
