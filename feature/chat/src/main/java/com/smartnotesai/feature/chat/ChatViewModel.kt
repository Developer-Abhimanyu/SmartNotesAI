package com.smartnotesai.feature.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnotesai.domain.model.AiError
import com.smartnotesai.domain.model.ChatConversation
import com.smartnotesai.domain.model.ChatMessage
import com.smartnotesai.domain.model.ChatRole
import com.smartnotesai.domain.usecase.GenerateChatResponseStreamUseCase
import com.smartnotesai.domain.usecase.GenerateImageUnderstandingStreamUseCase
import com.smartnotesai.domain.usecase.GetAllConversationsUseCase
import com.smartnotesai.domain.usecase.GetChatMessagesUseCase
import com.smartnotesai.domain.usecase.SaveChatMessageUseCase
import com.smartnotesai.domain.usecase.SaveConversationUseCase
import com.smartnotesai.feature.chat.di.IoDispatcher
import com.smartnotesai.feature.chat.image.ImageUriProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val generateChatResponseStreamUseCase:
    GenerateChatResponseStreamUseCase,
    private val generateImageUnderstandingStreamUseCase:
    GenerateImageUnderstandingStreamUseCase,
    private val imageUriProcessor: ImageUriProcessor,
    private val saveConversationUseCase:
    SaveConversationUseCase,
    private val saveChatMessageUseCase:
    SaveChatMessageUseCase,
    private val getAllConversationsUseCase:
    GetAllConversationsUseCase,
    private val getChatMessagesUseCase:
    GetChatMessagesUseCase,
    @param:IoDispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var streamingJob: Job? = null
    private var conversationsJob: Job? = null
    private var messagesJob: Job? = null

    init {
        observeConversations()
    }

    fun onAction(action: ChatUiAction) {
        when (action) {
            is ChatUiAction.PromptChanged -> {
                _uiState.update {
                    it.copy(
                        prompt = action.prompt,
                        errorMessage = null
                    )
                }
            }

            is ChatUiAction.ImageSelected -> {
                _uiState.update {
                    it.copy(
                        selectedImageUri = action.uri,
                        errorMessage = null
                    )
                }
            }

            is ChatUiAction.RemoveSelectedImage -> {
                _uiState.update {
                    it.copy(
                        selectedImageUri = null,
                        errorMessage = null
                    )
                }
            }

            is ChatUiAction.SendMessage -> sendMessage()

            is ChatUiAction.ClearConversation -> clearConversation()

            is ChatUiAction.ClearError -> {
                _uiState.update {
                    it.copy(errorMessage = null)
                }
            }

            is ChatUiAction.ConversationSelected -> {
                selectConversation(action.conversationId)
            }
        }
    }



    private fun observeConversations() {
        conversationsJob = viewModelScope.launch {
            getAllConversationsUseCase()
                .catch { exception ->
                    if (exception is CancellationException) {
                        throw exception
                    }

                    _uiState.update {
                        it.copy(
                            errorMessage = "Unable to load conversations. Please try again."
                        )
                    }
                }
                .collect { conversations ->
                    _uiState.update {
                        it.copy(conversations = conversations)
                    }
                }
        }
    }

    private fun selectConversation(conversationId: String) {
        if (conversationId.isBlank()) return
        if (_uiState.value.isStreaming) return

        val conversation = _uiState.value.conversations
            .find { it.id == conversationId } ?: return

        messagesJob?.cancel()

        _uiState.update {
            it.copy(
                conversation = conversation,
                messages = emptyList(),
                prompt = "",
                streamedResponse = "",
                selectedImageUri = null,
                errorMessage = null,
                isLoadingHistory = true
            )
        }

        messagesJob = viewModelScope.launch {
            try {
                getChatMessagesUseCase(conversationId).collect { messages ->
                    _uiState.update { state ->
                        if (state.conversation?.id == conversationId) {
                            state.copy(
                                messages = messages,
                                isLoadingHistory = false
                            )
                        } else {
                            state
                        }
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingHistory = false,
                        errorMessage = "Unable to load conversation history."
                    )
                }
            }
        }
    }

    private fun sendMessage() {
        val currentState = _uiState.value

        if (currentState.isStreaming || currentState.isLoadingHistory) {
            return
        }

        val selectedImageUri = currentState.selectedImageUri

        if (selectedImageUri != null) {
            sendImageMessage(selectedImageUri)
            return
        }

        val prompt = currentState.prompt.trim()

        if (prompt.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Please enter a message.")
            }
            return
        }

        val now = System.currentTimeMillis()

        val conversation = currentState.conversation?.copy(
            updatedAt = now
        ) ?: ChatConversation(
            id = UUID.randomUUID().toString(),
            title = prompt.take(50),
            createdAt = now,
            updatedAt = now
        )

        val history = currentState.messages

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = conversation.id,
            role = ChatRole.USER,
            content = prompt,
            createdAt = now
        )

        val assistantMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = conversation.id,
            role = ChatRole.ASSISTANT,
            content = "",
            createdAt = System.currentTimeMillis()
        )

        _uiState.update {
            it.copy(
                conversation = conversation,
                messages = it.messages + userMessage + assistantMessage,
                streamedResponse = "",
                isStreaming = true,
                errorMessage = null,
                prompt = ""
            )
        }

        streamingJob = viewModelScope.launch {
            try {
                try {
                    saveConversationUseCase(conversation)
                    saveChatMessageUseCase(userMessage)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    handleDatabaseError(
                        userMessageId = userMessage.id,
                        assistantMessageId = assistantMessage.id,
                        previousConversation = currentState.conversation,
                        previousPrompt = currentState.prompt
                    )
                    return@launch
                }

                generateChatResponseStreamUseCase(
                    history = history,
                    prompt = prompt
                ).collect { chunk ->
                    updateAssistantResponse(assistantMessage.id, chunk)
                }

                try {
                    saveCompletedAssistantMessage(assistantMessage)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    handleAssistantSaveError()
                }

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                handleStreamingError(exception, assistantMessage.id)
            } finally {
                _uiState.update {
                    it.copy(isStreaming = false)
                }
            }
        }
    }

    private fun handleAssistantSaveError() {
        _uiState.update {
            it.copy(
                errorMessage = "Unable to save your conversation. Please try again."
            )
        }
    }

    private fun sendImageMessage(uri: Uri) {
        val currentState = _uiState.value

        if (currentState.isStreaming || currentState.isLoadingHistory) {
            return
        }

        val prompt = currentState.prompt.trim()
            .ifBlank { "Describe this image in detail." }

        val now = System.currentTimeMillis()

        val conversation = currentState.conversation?.copy(
            updatedAt = now
        ) ?: ChatConversation(
            id = UUID.randomUUID().toString(),
            title = prompt.take(50),
            createdAt = now,
            updatedAt = now
        )

        // The database stores the text and an attachment marker.
        // The actual image bytes are not persisted.
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = conversation.id,
            role = ChatRole.USER,
            content = "[Image attached]\n$prompt",
            createdAt = now
        )

        val assistantMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = conversation.id,
            role = ChatRole.ASSISTANT,
            content = "",
            createdAt = System.currentTimeMillis()
        )

        _uiState.update {
            it.copy(
                conversation = conversation,
                messages = it.messages + userMessage + assistantMessage,
                streamedResponse = "",
                isStreaming = true,
                selectedImageUri = uri,
                prompt = "",
                errorMessage = null
            )
        }

        streamingJob = viewModelScope.launch {
            try {
                // Step 1: Save conversation and user message.
                try {
                    saveConversationUseCase(conversation)
                    saveChatMessageUseCase(userMessage)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    handleDatabaseError(
                        userMessageId = userMessage.id,
                        assistantMessageId = assistantMessage.id,
                        previousConversation = currentState.conversation,
                        previousPrompt = currentState.prompt,
                        selectedImageUri = uri
                    )
                    return@launch
                }

                // Step 2: Process image on the IO dispatcher.
                val imageInput = try {
                    withContext(ioDispatcher) {
                        imageUriProcessor.process(uri)
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    _uiState.update {
                        it.copy(
                            errorMessage = "Unable to process the selected image. Please try another image."
                        )
                    }
                    return@launch
                }
                _uiState.update {
                    it.copy(selectedImageUri = null)
                }

                // Step 3: Generate AI response.
                generateImageUnderstandingStreamUseCase(
                    image = imageInput,
                    prompt = prompt
                ).collect { chunk ->
                    updateAssistantResponse(assistantMessage.id, chunk)
                }

                // Step 4: Save completed AI response.
                try {
                    saveCompletedAssistantMessage(assistantMessage)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    handleAssistantSaveError()
                }

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                handleStreamingError(exception, assistantMessage.id)
            } finally {
                _uiState.update {
                    it.copy(isStreaming = false)
                }
            }
        }
    }

    private fun updateAssistantResponse(
        assistantMessageId: String,
        chunk: String
    ) {
        _uiState.update { state ->
            val updatedResponse = state.streamedResponse + chunk

            state.copy(
                streamedResponse = updatedResponse,
                messages = state.messages.map { message ->
                    if (message.id == assistantMessageId) {
                        message.copy(content = updatedResponse)
                    } else {
                        message
                    }
                }
            )
        }
    }

    private suspend fun saveCompletedAssistantMessage(
        assistantMessage: ChatMessage
    ) {
        val completedResponse = _uiState.value.streamedResponse

        if (completedResponse.isNotBlank()) {
            saveChatMessageUseCase(
                assistantMessage.copy(content = completedResponse)
            )
        }
    }

    private fun handleStreamingError(
        exception: Exception,
        assistantMessageId: String
    ) {
        val message = (exception as? AiError)?.toUserMessage()
            ?: "Unable to generate a response. Please try again."

        _uiState.update { state ->
            state.copy(
                isStreaming = false,
                errorMessage = message,
                messages = state.messages.filterNot {
                    it.id == assistantMessageId && it.content.isEmpty()
                }
            )
        }
    }

    private fun clearConversation() {
        streamingJob?.cancel()
        streamingJob = null

        messagesJob?.cancel()
        messagesJob = null

        _uiState.update { state ->
            ChatUiState(
                conversations = state.conversations
            )
        }
    }


    private fun handleDatabaseError(
        userMessageId: String,
        assistantMessageId: String,
        previousConversation: ChatConversation?,
        previousPrompt: String,
        selectedImageUri: Uri? = null
    ) {
        _uiState.update { state ->
            state.copy(
                conversation = previousConversation,
                messages = state.messages.filterNot {
                    it.id == userMessageId ||
                            it.id == assistantMessageId
                },
                prompt = previousPrompt,
                selectedImageUri = selectedImageUri,
                streamedResponse = "",
                errorMessage = "Unable to save your conversation. Please try again."
            )
        }
    }
}

private fun AiError.toUserMessage(): String {
    return when (this) {
        is AiError.Network ->
            "Check your internet connection and try again."

        is AiError.RateLimit ->
            "AI service limit reached. Please try later."

        is AiError.Timeout ->
            "The AI request timed out. Please try again."

        is AiError.SafetyBlocked ->
            "This request could not be processed."

        is AiError.Configuration ->
            "AI service is not configured correctly."

        is AiError.Server ->
            "AI service is temporarily unavailable."

        is AiError.Unknown ->
            "Something went wrong. Please try again."
    }
}
