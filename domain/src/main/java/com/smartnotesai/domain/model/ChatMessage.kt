package com.smartnotesai.domain.model

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val role: ChatRole,
    val content: String,
    val createdAt: Long
)