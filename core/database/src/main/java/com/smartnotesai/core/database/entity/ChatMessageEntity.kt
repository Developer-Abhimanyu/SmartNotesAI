package com.smartnotesai.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationId"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val role: String,
    val content: String,
    val createdAt: Long
)
