package com.smartnotesai.domain.usecase

import com.smartnotesai.domain.model.ChatMessage
import com.smartnotesai.domain.model.ChatRole
import com.smartnotesai.domain.repository.AiRepository
import com.smartnotesai.domain.usecase.validation.AiInputValidator
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GenerateChatResponseStreamUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {

    operator fun invoke(
        history: List<ChatMessage>,
        prompt: String
    ): Flow<String> {

        AiInputValidator.validatePrompt(prompt)

        require(history.all { it.content.isNotBlank() }) {
            "Conversation history contains an empty message."
        }

        require(
            history.none {
                it.role == ChatRole.USER && it.content.isBlank()
            }
        ) {
            "Conversation history contains an invalid user message."
        }

        return aiRepository.generateChatResponseStream(
            history = history,
            prompt = prompt
        )
    }
}
