package com.smartnotesai.data.repository

import com.smartnotesai.core.database.dao.ChatConversationDao
import com.smartnotesai.core.database.dao.ChatMessageDao
import com.smartnotesai.core.database.entity.ChatConversationEntity
import com.smartnotesai.core.database.entity.ChatMessageEntity
import com.smartnotesai.domain.model.ChatConversation
import com.smartnotesai.domain.model.ChatMessage
import com.smartnotesai.domain.model.ChatRole
import com.smartnotesai.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val conversationDao: ChatConversationDao,
    private val messageDao: ChatMessageDao
) : ChatRepository {

    override fun getAllConversations(): Flow<List<ChatConversation>> {
        return conversationDao.getAllConversations().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getConversationById(
        conversationId: String
    ): ChatConversation? {
        return conversationDao.getConversationById(conversationId)
            ?.toDomain()
    }

    override fun getMessages(
        conversationId: String
    ): Flow<List<ChatMessage>> {
        return messageDao.getMessagesForConversation(conversationId)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun getMessagesOnce(
        conversationId: String
    ): List<ChatMessage> {
        return messageDao.getMessagesForConversationOnce(conversationId)
            .map { it.toDomain() }
    }

    override suspend fun saveConversation(
        conversation: ChatConversation
    ) {
        conversationDao.insertConversation(conversation.toEntity())
    }

    override suspend fun saveMessage(
        message: ChatMessage
    ) {
        messageDao.insertMessage(message.toEntity())
    }

    override suspend fun deleteConversation(
        conversationId: String
    ) {
        conversationDao.deleteConversation(conversationId)
    }

    private fun ChatConversationEntity.toDomain() = ChatConversation(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun ChatConversation.toEntity() = ChatConversationEntity(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun ChatMessageEntity.toDomain() = ChatMessage(
        id = id,
        role = ChatRole.valueOf(role),
        conversationId = conversationId,
        content = content,
        createdAt = createdAt
    )

    private fun ChatMessage.toEntity() = ChatMessageEntity(
        id = id,
        conversationId = conversationId,
        role = role.name,
        content = content,
        createdAt = createdAt
    )
}
