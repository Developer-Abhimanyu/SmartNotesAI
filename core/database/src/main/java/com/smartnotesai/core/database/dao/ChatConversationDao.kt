package com.smartnotesai.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smartnotesai.core.database.entity.ChatConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatConversationDao {

    @Query(
        "SELECT * FROM chat_conversations ORDER BY updatedAt DESC"
    )
    fun getAllConversations(): Flow<List<ChatConversationEntity>>

    @Query(
        "SELECT * FROM chat_conversations WHERE id = :id"
    )
    suspend fun getConversationById(
        id: String
    ): ChatConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(
        conversation: ChatConversationEntity
    )

    @Update
    suspend fun updateConversation(
        conversation: ChatConversationEntity
    )

    @Query(
        "DELETE FROM chat_conversations WHERE id = :id"
    )
    suspend fun deleteConversation(id: String)
}
