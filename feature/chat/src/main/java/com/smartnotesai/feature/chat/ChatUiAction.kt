package com.smartnotesai.feature.chat

import android.net.Uri

sealed interface ChatUiAction {

    data class PromptChanged(
        val prompt: String
    ) : ChatUiAction

    data class ImageSelected(
        val uri: Uri
    ) : ChatUiAction

    data object RemoveSelectedImage : ChatUiAction

    data object SendMessage : ChatUiAction

    data object ClearConversation : ChatUiAction

    data object ClearError : ChatUiAction

    data class ConversationSelected(
        val conversationId: String
    ) : ChatUiAction
}