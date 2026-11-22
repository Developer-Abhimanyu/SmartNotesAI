package com.smartnotesai.feature.chat

import android.net.Uri
import com.smartnotesai.domain.model.ChatConversation
import com.smartnotesai.domain.model.ChatMessage

data class ChatUiState(
    val prompt: String = "",
    val conversations: List<ChatConversation> = emptyList(),
    val conversation: ChatConversation? = null,
    val messages: List<ChatMessage> = emptyList(),
    val streamedResponse: String = "",
    val isStreaming: Boolean = false,
    val isLoadingHistory: Boolean = false,
    val errorMessage: String? = null,
    val selectedImageUri: Uri? = null
)
