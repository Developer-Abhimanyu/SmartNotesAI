package com.smartnotesai.feature.chat

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.smartnotesai.domain.model.ChatConversation
import com.smartnotesai.domain.model.ChatMessage
import com.smartnotesai.domain.model.ChatRole
import com.smartnotesai.domain.model.ImageInput
import com.smartnotesai.domain.repository.AiRepository
import com.smartnotesai.domain.repository.ChatRepository
import com.smartnotesai.domain.usecase.GenerateChatResponseStreamUseCase
import com.smartnotesai.domain.usecase.GenerateImageUnderstandingStreamUseCase
import com.smartnotesai.domain.usecase.GetAllConversationsUseCase
import com.smartnotesai.domain.usecase.GetChatMessagesUseCase
import com.smartnotesai.domain.usecase.SaveChatMessageUseCase
import com.smartnotesai.domain.usecase.SaveConversationUseCase
import com.smartnotesai.feature.chat.image.ImageUriProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher: TestDispatcher =
        StandardTestDispatcher()

    private lateinit var fakeAiRepository: FakeAiRepository
    private lateinit var fakeChatRepository: FakeChatRepository
    private lateinit var viewModel: ChatViewModel

    private lateinit var testContext: Context
    private lateinit var testImageFile: File
    private lateinit var testImageProvider: TestImageContentProvider

    private val testImageUri =
        Uri.parse("content://com.smartnotesai.chat.test/image")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        fakeAiRepository = FakeAiRepository()
        fakeChatRepository = FakeChatRepository()

        testContext = RuntimeEnvironment.getApplication()
        testImageFile = File.createTempFile("chat-test-image", ".jpg")
        testImageProvider = TestImageContentProvider(
            file = testImageFile,
            mimeType = "image/jpeg"
        )

        val providerInfo = ProviderInfo().apply {
            authority = "com.smartnotesai.chat.test"
            exported = true
            grantUriPermissions = true
        }

        testImageProvider.attachInfo(testContext, providerInfo)

        ShadowContentResolver.registerProviderInternal(
            "com.smartnotesai.chat.test",
            testImageProvider
        )

        viewModel = createViewModel()
    }

    @After
    fun tearDown() {
        testImageFile.delete()
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ChatViewModel(
        generateChatResponseStreamUseCase =
            GenerateChatResponseStreamUseCase(fakeAiRepository),

        generateImageUnderstandingStreamUseCase =
            GenerateImageUnderstandingStreamUseCase(fakeAiRepository),

        imageUriProcessor =
            ImageUriProcessor(RuntimeEnvironment.getApplication()),

        // Keep image processing on the coroutine test scheduler.
        ioDispatcher = testDispatcher,

        saveConversationUseCase =
            SaveConversationUseCase(fakeChatRepository),

        saveChatMessageUseCase =
            SaveChatMessageUseCase(fakeChatRepository),

        getAllConversationsUseCase =
            GetAllConversationsUseCase(fakeChatRepository),

        getChatMessagesUseCase =
            GetChatMessagesUseCase(fakeChatRepository),
    )

    @Test
    fun `sending image streams AI response and saves messages`() = runTest {
        // Arrange
        val imageBytes = byteArrayOf(10, 20, 30, 40, 50)
        testImageFile.writeBytes(imageBytes)

        fakeAiRepository.imageStream = flowOf(
            "This image ",
            "contains a test object."
        )

        // Act
        viewModel.onAction(
            ChatUiAction.ImageSelected(testImageUri)
        )

        viewModel.onAction(
            ChatUiAction.PromptChanged("Describe this image")
        )

        viewModel.onAction(
            ChatUiAction.SendMessage
        )

        advanceUntilIdle()

        // Assert: AI image API was called
        assertEquals(
            1,
            fakeAiRepository.imageStreamCallCount
        )

        // Assert: image bytes and MIME type were forwarded
        assertArrayEquals(
            imageBytes,
            fakeAiRepository.lastImage?.bytes
        )

        assertEquals(
            "image/jpeg",
            fakeAiRepository.lastImage?.mimeType
        )

        // Assert: prompt was forwarded
        assertEquals(
            "Describe this image",
            fakeAiRepository.lastImagePrompt
        )

        // Assert: streamed response was received
        assertEquals(
            "This image contains a test object.",
            viewModel.uiState.value.streamedResponse
        )

        assertFalse(viewModel.uiState.value.isStreaming)
        assertNull(viewModel.uiState.value.errorMessage)

        // Assert: conversation and messages were persisted
        assertEquals(1, fakeChatRepository.conversations.size)
        assertEquals(2, fakeChatRepository.messages.size)

        assertEquals(
            "[Image attached]\nDescribe this image",
            fakeChatRepository.messages[0].content
        )

        assertEquals(
            "This image contains a test object.",
            fakeChatRepository.messages[1].content
        )

        // Assert: selected image was cleared after sending
        assertNull(viewModel.uiState.value.selectedImageUri)
    }

    @Test
    fun `prompt changed updates UI state`() = runTest {
        viewModel.onAction(
            ChatUiAction.PromptChanged("Explain Kotlin Flow")
        )

        assertEquals(
            "Explain Kotlin Flow",
            viewModel.uiState.value.prompt
        )

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `blank prompt displays validation error`() = runTest {
        viewModel.onAction(ChatUiAction.SendMessage)

        assertEquals(
            "Please enter a message.",
            viewModel.uiState.value.errorMessage
        )

        assertEquals(0, fakeAiRepository.streamCallCount)
    }

    @Test
    fun `send message saves conversation and messages`() = runTest {
        fakeAiRepository.stream = flowOf(
            "Kotlin ",
            "Flow ",
            "is powerful."
        )

        viewModel.onAction(
            ChatUiAction.PromptChanged("Explain Kotlin Flow")
        )
        viewModel.onAction(ChatUiAction.SendMessage)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(
            "Kotlin Flow is powerful.",
            state.streamedResponse
        )
        assertFalse(state.isStreaming)
        assertNull(state.errorMessage)

        assertEquals(2, state.messages.size)
        assertEquals(1, fakeChatRepository.conversations.size)
        assertEquals(2, fakeChatRepository.messages.size)

        assertEquals(
            state.conversation?.id,
            fakeChatRepository.messages[0].conversationId
        )
    }

    @Test
    fun `second message reuses existing conversation`() = runTest {
        fakeAiRepository.stream = flowOf("First response")

        viewModel.onAction(
            ChatUiAction.PromptChanged("First question")
        )
        viewModel.onAction(ChatUiAction.SendMessage)

        advanceUntilIdle()

        val firstConversationId =
            viewModel.uiState.value.conversation?.id

        fakeAiRepository.stream = flowOf("Second response")

        viewModel.onAction(
            ChatUiAction.PromptChanged("Second question")
        )
        viewModel.onAction(ChatUiAction.SendMessage)

        advanceUntilIdle()

        assertEquals(
            firstConversationId,
            viewModel.uiState.value.conversation?.id
        )

        assertEquals(1, fakeChatRepository.conversations.size)
        assertEquals(4, fakeChatRepository.messages.size)
    }

    @Test
    fun `saved conversations are observed on initialization`() = runTest {
        val conversation = createConversation()
        fakeChatRepository.conversations.add(conversation)

        viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(
            listOf(conversation),
            viewModel.uiState.value.conversations
        )
    }

    @Test
    fun `selecting conversation restores its messages`() = runTest {
        val conversation = createConversation()
        val messages = listOf(
            createMessage(
                conversationId = conversation.id,
                role = ChatRole.USER,
                content = "Explain Kotlin Coroutines"
            ),
            createMessage(
                conversationId = conversation.id,
                role = ChatRole.ASSISTANT,
                content = "Coroutines are lightweight tasks."
            )
        )

        fakeChatRepository.conversations.add(conversation)
        fakeChatRepository.messages.addAll(messages)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(
            ChatUiAction.ConversationSelected(conversation.id)
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(conversation, state.conversation)
        assertEquals(messages, state.messages)
        assertFalse(state.isLoadingHistory)
        assertNull(state.errorMessage)
    }

    @Test
    fun `restored conversation can continue with previous history`() = runTest {
        val conversation = createConversation()
        val previousMessage = createMessage(
            conversationId = conversation.id,
            role = ChatRole.USER,
            content = "Explain Kotlin Coroutines"
        )

        fakeChatRepository.conversations.add(conversation)
        fakeChatRepository.messages.add(previousMessage)
        fakeAiRepository.stream = flowOf("Here is more detail.")

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(
            ChatUiAction.ConversationSelected(conversation.id)
        )
        advanceUntilIdle()

        viewModel.onAction(
            ChatUiAction.PromptChanged("Give me an Android example")
        )
        viewModel.onAction(ChatUiAction.SendMessage)

        advanceUntilIdle()

        assertEquals(
            previousMessage.content,
            fakeAiRepository.lastHistory.first().content
        )
        assertEquals(1, fakeAiRepository.lastHistory.size)
        assertEquals(3, fakeChatRepository.messages.size)
    }


    @Test
    fun `assistant message database failure displays database error`() = runTest {
        // Arrange
        fakeAiRepository.stream = flowOf("Kotlin Flow is powerful.")

        // First call saves the user message successfully.
        // Second call saves the completed assistant message and fails.
        fakeChatRepository.saveMessageException =
            IllegalStateException("Assistant message write failed")

        fakeChatRepository.failOnSaveMessageCall = 2

        viewModel.onAction(
            ChatUiAction.PromptChanged("Explain Kotlin Flow")
        )

        // Act
        viewModel.onAction(ChatUiAction.SendMessage)

        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value

        assertEquals(
            "Unable to save your conversation. Please try again.",
            state.errorMessage
        )

        assertEquals(
            "Kotlin Flow is powerful.",
            state.streamedResponse
        )

        assertEquals(2, fakeChatRepository.saveMessageCallCount)
        assertEquals(1, fakeAiRepository.streamCallCount)
        assertFalse(state.isStreaming)
    }


    @Test
    fun `image message database failure displays database error`() = runTest {
        // Arrange
        testImageFile.writeBytes(byteArrayOf(10, 20, 30))
        fakeChatRepository.saveConversationException =
            IllegalStateException("Database write failed")

        viewModel.onAction(
            ChatUiAction.ImageSelected(testImageUri)
        )

        // Act
        viewModel.onAction(ChatUiAction.SendMessage)

        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value

        assertEquals(
            "Unable to save your conversation. Please try again.",
            state.errorMessage
        )

        assertEquals(0, fakeAiRepository.imageStreamCallCount)
        assertFalse(state.isStreaming)

        assertEquals(
            testImageUri,
            state.selectedImageUri
        )
    }

    @Test
    fun `image processing failure preserves selected image and displays error`() = runTest {
        // Arrange
        testImageFile.writeBytes(byteArrayOf(1, 2, 3))
        testImageProvider.mimeType = "image/gif"

        viewModel.onAction(
            ChatUiAction.ImageSelected(testImageUri)
        )

        // Act
        viewModel.onAction(ChatUiAction.SendMessage)

        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value

        assertEquals(
            "Unable to process the selected image. Please try another image.",
            state.errorMessage
        )

        assertEquals(testImageUri, state.selectedImageUri)
        assertEquals(0, fakeAiRepository.imageStreamCallCount)
        assertFalse(state.isStreaming)
    }

    @Test
    fun `streaming failure resets loading state`() = runTest {
        fakeAiRepository.stream = flow {
            throw IllegalStateException("Test failure")
        }

        viewModel.onAction(
            ChatUiAction.PromptChanged("Generate a response")
        )
        viewModel.onAction(ChatUiAction.SendMessage)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(
            "Unable to generate a response. Please try again.",
            state.errorMessage
        )
        assertFalse(state.isStreaming)

        assertEquals(1, fakeChatRepository.messages.size)
        assertEquals(
            ChatRole.USER,
            fakeChatRepository.messages.first().role
        )
    }


    @Test
    fun `database failure while saving user message displays error`() = runTest {
        // Arrange
        fakeChatRepository.saveMessageException =
            IllegalStateException("Message write failed")

        viewModel.onAction(
            ChatUiAction.PromptChanged("Explain Kotlin Flow")
        )

        // Act
        viewModel.onAction(ChatUiAction.SendMessage)

        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value

        assertEquals(
            "Unable to save your conversation. Please try again.",
            state.errorMessage
        )

        assertFalse(state.isStreaming)
        assertEquals(0, fakeAiRepository.streamCallCount)

        // Conversation was saved, but message was not.
        assertEquals(1, fakeChatRepository.conversations.size)
        assertTrue(fakeChatRepository.messages.isEmpty())
    }

    @Test
    fun `database failure while saving conversation displays error`() = runTest {
        // Arrange
        fakeChatRepository.saveConversationException =
            IllegalStateException("Database write failed")

        viewModel.onAction(
            ChatUiAction.PromptChanged("Explain Kotlin Flow")
        )

        // Act
        viewModel.onAction(ChatUiAction.SendMessage)

        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value

        assertEquals(
            "Unable to save your conversation. Please try again.",
            state.errorMessage
        )

        assertFalse(state.isStreaming)

        // AI generation should not start.
        assertEquals(0, fakeAiRepository.streamCallCount)
    }

    @Test
    fun `clear conversation resets UI but preserves saved history`() = runTest {
        fakeAiRepository.stream = flowOf("Response")

        viewModel.onAction(
            ChatUiAction.PromptChanged("Hello")
        )
        viewModel.onAction(ChatUiAction.SendMessage)

        advanceUntilIdle()

        viewModel.onAction(ChatUiAction.ClearConversation)

        val state = viewModel.uiState.value

        assertTrue(state.messages.isEmpty())
        assertNull(state.conversation)
        assertEquals("", state.prompt)

        assertEquals(1, fakeChatRepository.conversations.size)
        assertEquals(2, fakeChatRepository.messages.size)
    }

    private fun createConversation() = ChatConversation(
        id = "conversation-1",
        title = "Kotlin Coroutines",
        createdAt = 1000L,
        updatedAt = 2000L
    )

    private fun createMessage(
        conversationId: String,
        role: ChatRole,
        content: String
    ) = ChatMessage(
        id = "message-${fakeChatRepository.messages.size + 1}",
        conversationId = conversationId,
        role = role,
        content = content,
        createdAt = 3000L
    )

    private class FakeAiRepository : AiRepository {

        var imageStreamCallCount = 0
        var lastImage: ImageInput? = null
        var lastImagePrompt: String? = null

        var imageStream: Flow<String> = flowOf(
            "This image contains a test object."
        )

        var streamCallCount = 0
        var lastHistory: List<ChatMessage> = emptyList()

        var stream: Flow<String> = flowOf("Default response")

        override suspend fun generateText(prompt: String) =
            "Default response"

        override fun generateTextStream(prompt: String): Flow<String> {
            streamCallCount++
            return stream
        }

        override fun generateChatResponseStream(
            history: List<ChatMessage>,
            prompt: String
        ): Flow<String> {
            streamCallCount++
            lastHistory = history
            return stream
        }

        override fun generateImageUnderstandingStream(
            image: ImageInput,
            prompt: String
        ): Flow<String> {
            imageStreamCallCount++
            lastImage = image
            lastImagePrompt = prompt
            return imageStream
        }
    }

    private class FakeChatRepository : ChatRepository {

        var failOnSaveMessageCall: Int? = null

        var saveMessageCallCount = 0

        var saveMessageException: Exception? = null

        val conversations = mutableListOf<ChatConversation>()
        val messages = mutableListOf<ChatMessage>()
        var saveConversationException: Exception? = null

        override fun getAllConversations():
                Flow<List<ChatConversation>> {
            return flowOf(conversations.toList())
        }

        override suspend fun getConversationById(
            conversationId: String
        ): ChatConversation? {
            return conversations.find { it.id == conversationId }
        }

        override fun getMessages(
            conversationId: String
        ): Flow<List<ChatMessage>> {
            return flowOf(
                messages.filter {
                    it.conversationId == conversationId
                }
            )
        }

        override suspend fun getMessagesOnce(
            conversationId: String
        ): List<ChatMessage> {
            return messages.filter {
                it.conversationId == conversationId
            }
        }

        override suspend fun saveConversation(
            conversation: ChatConversation
        ) {
            saveConversationException?.let {
                throw it
            }
            val index = conversations.indexOfFirst {
                it.id == conversation.id
            }

            if (index == -1) {
                conversations.add(conversation)
            } else {
                conversations[index] = conversation
            }
        }

        override suspend fun saveMessage(message: ChatMessage) {
            saveMessageCallCount++

            if (saveMessageCallCount == failOnSaveMessageCall) {
                throw saveMessageException
                    ?: IllegalStateException("Message write failed")
            }

            saveMessageException?.let {
                if (failOnSaveMessageCall == null) {
                    throw it
                }
            }

            messages.add(message)
        }

        override suspend fun deleteConversation(conversationId: String) {
            conversations.removeAll { it.id == conversationId }
            messages.removeAll {
                it.conversationId == conversationId
            }
        }
    }

    @Test
    fun `selecting image updates UI state`() = runTest {
        val imageUri = Uri.parse("content://test/images/photo.jpg")

        viewModel.onAction(
            ChatUiAction.ImageSelected(imageUri)
        )

        assertEquals(
            imageUri,
            viewModel.uiState.value.selectedImageUri
        )

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `removing selected image clears UI state`() = runTest {
        val imageUri = Uri.parse("content://test/images/photo.jpg")

        viewModel.onAction(
            ChatUiAction.ImageSelected(imageUri)
        )

        viewModel.onAction(
            ChatUiAction.RemoveSelectedImage
        )

        assertNull(
            viewModel.uiState.value.selectedImageUri
        )
    }
}

private class TestImageContentProvider(
    private val file: File,
    var mimeType: String
) : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String = mimeType

    override fun openFile(
        uri: Uri,
        mode: String
    ): ParcelFileDescriptor {
        return ParcelFileDescriptor.open(
            file,
            ParcelFileDescriptor.MODE_READ_ONLY
        )
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun insert(
        uri: Uri,
        values: ContentValues?
    ): Uri? = null

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
