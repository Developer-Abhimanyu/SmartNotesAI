package com.smartnotesai.domain.usecase

import com.smartnotesai.domain.model.ChatMessage
import com.smartnotesai.domain.repository.ChatRepository
import javax.inject.Inject

class SaveChatMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(message: ChatMessage) {
        require(message.id.isNotBlank()) {
            "Message ID cannot be empty."
        }

        require(message.conversationId.isNotBlank()) {
            "Conversation ID cannot be empty."
        }

        require(message.content.isNotBlank()) {
            "Message content cannot be empty."
        }

        chatRepository.saveMessage(message)
    }
}
