package com.smartnotesai.data.ai.streaming

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.smartnotesai.domain.model.ChatMessage
import com.smartnotesai.domain.model.ChatRole
import com.smartnotesai.domain.model.ImageInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseGeminiStreamingClient @Inject constructor() :
    GeminiStreamingClient {

    private val generativeModel by lazy {
        Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-3.5-flash-lite")
    }

    override fun generateContentStream(
        prompt: String
    ): Flow<String> {
        return generativeModel
            .generateContentStream(prompt)
            .mapNotNull { response ->
                response.text?.takeIf { it.isNotEmpty() }
            }
    }

    override fun generateChatResponseStream(
        history: List<ChatMessage>,
        prompt: String
    ): Flow<String> {

        val firebaseHistory = history.map { message ->
            val firebaseRole = when (message.role) {
                ChatRole.USER -> "user"
                ChatRole.ASSISTANT -> "model"
            }

            content(role = firebaseRole) {
                text(message.content)
            }
        }

        val chat = generativeModel.startChat(
            history = firebaseHistory
        )

        return chat.sendMessageStream(prompt)
            .mapNotNull { response ->
                response.text?.takeIf { it.isNotEmpty() }
            }
    }

    override fun generateImageUnderstandingStream(
        image: ImageInput,
        prompt: String
    ): Flow<String> {

        val multimodalPrompt = content {
            inlineData(
                image.bytes,
                image.mimeType
            )
            text(prompt)
        }

        return generativeModel
            .generateContentStream(multimodalPrompt)
            .mapNotNull { response ->
                response.text?.takeIf { it.isNotEmpty() }
            }
    }
}
