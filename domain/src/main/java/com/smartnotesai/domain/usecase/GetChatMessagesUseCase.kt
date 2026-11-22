package com.smartnotesai.domain.usecase

import com.smartnotesai.domain.model.ChatMessage
import com.smartnotesai.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChatMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(
        conversationId: String
    ): Flow<List<ChatMessage>> {
        require(conversationId.isNotBlank()) {
            "Conversation ID cannot be empty."
        }

        return chatRepository.getMessages(conversationId)
    }
}
