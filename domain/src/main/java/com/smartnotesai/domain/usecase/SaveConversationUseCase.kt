package com.smartnotesai.domain.usecase

import com.smartnotesai.domain.model.ChatConversation
import com.smartnotesai.domain.repository.ChatRepository
import javax.inject.Inject

class SaveConversationUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(conversation: ChatConversation) {
        require(conversation.id.isNotBlank()) {
            "Conversation ID cannot be empty."
        }

        require(conversation.title.isNotBlank()) {
            "Conversation title cannot be empty."
        }

        chatRepository.saveConversation(conversation)
    }
}
