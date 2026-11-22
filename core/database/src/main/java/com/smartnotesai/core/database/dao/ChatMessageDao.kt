package com.smartnotesai.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartnotesai.core.database.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    @Query(
        """
        SELECT * FROM chat_messages
        WHERE conversationId = :conversationId
        ORDER BY createdAt ASC
        """
    )
    fun getMessagesForConversation(
        conversationId: String
    ): Flow<List<ChatMessageEntity>>

    @Query(
        """
        SELECT * FROM chat_messages
        WHERE conversationId = :conversationId
        ORDER BY createdAt ASC
        """
    )
    suspend fun getMessagesForConversationOnce(
        conversationId: String
    ): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query(
        "DELETE FROM chat_messages WHERE conversationId = :conversationId"
    )
    suspend fun deleteMessagesForConversation(conversationId: String)
}
