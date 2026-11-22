package com.smartnotesai.domain.repository

import com.smartnotesai.domain.model.ChatConversation
import com.smartnotesai.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun getAllConversations(): Flow<List<ChatConversation>>

    suspend fun getConversationById(
        conversationId: String
    ): ChatConversation?

    fun getMessages(
        conversationId: String
    ): Flow<List<ChatMessage>>

    suspend fun getMessagesOnce(
        conversationId: String
    ): List<ChatMessage>

    suspend fun saveConversation(
        conversation: ChatConversation
    )

    suspend fun saveMessage(
        message: ChatMessage
    )

    suspend fun deleteConversation(
        conversationId: String
    )
}
