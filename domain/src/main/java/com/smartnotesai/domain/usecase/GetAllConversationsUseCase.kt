package com.smartnotesai.domain.usecase

import com.smartnotesai.domain.model.ChatConversation
import com.smartnotesai.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllConversationsUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(): Flow<List<ChatConversation>> {
        return chatRepository.getAllConversations()
    }
}
