package com.smartnotesai.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.smartnotesai.core.database.dao.ChatConversationDao
import com.smartnotesai.core.database.dao.ChatMessageDao
import com.smartnotesai.core.database.entity.ChatConversationEntity
import com.smartnotesai.core.database.entity.ChatMessageEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatMessageDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var conversationDao: ChatConversationDao
    private lateinit var messageDao: ChatMessageDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        conversationDao = database.chatConversationDao()
        messageDao = database.chatMessageDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun insertConversation(id: String) {
        conversationDao.insertConversation(
            ChatConversationEntity(
                id = id,
                title = "Test Conversation",
                createdAt = 1000L,
                updatedAt = 2000L
            )
        )
    }

    private fun createMessage(
        id: String,
        conversationId: String,
        content: String,
        createdAt: Long
    ) = ChatMessageEntity(
        id = id,
        conversationId = conversationId,
        role = "user",
        content = content,
        createdAt = createdAt
    )

    @Test
    fun insertMessage_andRetrieveMessagesForConversation() = runTest {
        insertConversation("conversation-1")

        val message = createMessage(
            id = "message-1",
            conversationId = "conversation-1",
            content = "Hello AI",
            createdAt = 1000L
        )

        messageDao.insertMessage(message)

        val result = messageDao.getMessagesForConversationOnce("conversation-1")

        assertEquals(1, result.size)
        assertEquals(message, result.first())
    }

    @Test
    fun getMessagesForConversation_returnsMessagesInChronologicalOrder() =
        runTest {
            insertConversation("conversation-1")

            val laterMessage = createMessage(
                id = "message-2",
                conversationId = "conversation-1",
                content = "Second message",
                createdAt = 2000L
            )

            val earlierMessage = createMessage(
                id = "message-1",
                conversationId = "conversation-1",
                content = "First message",
                createdAt = 1000L
            )

            messageDao.insertMessage(laterMessage)
            messageDao.insertMessage(earlierMessage)

            val result = messageDao
                .getMessagesForConversation("conversation-1")
                .first()

            assertEquals(2, result.size)
            assertEquals("message-1", result[0].id)
            assertEquals("message-2", result[1].id)
        }

    @Test
    fun getMessagesForConversation_returnsOnlyMessagesForRequestedConversation() =
        runTest {
            insertConversation("conversation-1")
            insertConversation("conversation-2")

            messageDao.insertMessage(
                createMessage(
                    "message-1",
                    "conversation-1",
                    "First conversation",
                    1000L
                )
            )

            messageDao.insertMessage(
                createMessage(
                    "message-2",
                    "conversation-2",
                    "Second conversation",
                    2000L
                )
            )

            val result = messageDao
                .getMessagesForConversationOnce("conversation-1")

            assertEquals(1, result.size)
            assertEquals("message-1", result.first().id)
        }

    @Test
    fun insertMessages_insertsMultipleMessages() = runTest {
        insertConversation("conversation-1")

        val messages = listOf(
            createMessage(
                "message-1",
                "conversation-1",
                "First message",
                1000L
            ),
            createMessage(
                "message-2",
                "conversation-1",
                "Second message",
                2000L
            )
        )

        messageDao.insertMessages(messages)

        val result = messageDao
            .getMessagesForConversationOnce("conversation-1")

        assertEquals(2, result.size)
    }

    @Test
    fun deleteMessagesForConversation_removesOnlyRequestedConversationMessages() =
        runTest {
            insertConversation("conversation-1")
            insertConversation("conversation-2")

            messageDao.insertMessages(
                listOf(
                    createMessage(
                        "message-1",
                        "conversation-1",
                        "First message",
                        1000L
                    ),
                    createMessage(
                        "message-2",
                        "conversation-2",
                        "Second message",
                        2000L
                    )
                )
            )

            messageDao.deleteMessagesForConversation("conversation-1")

            val firstConversationMessages =
                messageDao.getMessagesForConversationOnce("conversation-1")

            val secondConversationMessages =
                messageDao.getMessagesForConversationOnce("conversation-2")

            assertTrue(firstConversationMessages.isEmpty())
            assertEquals(1, secondConversationMessages.size)
        }


    @Test
    fun deletingConversation_alsoDeletesItsMessages() = runTest {
        insertConversation("conversation-1")

        val message = createMessage(
            id = "message-1",
            conversationId = "conversation-1",
            content = "This message should be deleted",
            createdAt = 1000L
        )

        messageDao.insertMessage(message)

        // Verify the message exists before deleting the conversation.
        val messagesBeforeDelete =
            messageDao.getMessagesForConversationOnce("conversation-1")

        assertEquals(1, messagesBeforeDelete.size)

        // Delete the parent conversation.
        conversationDao.deleteConversation("conversation-1")

        // The related message should be deleted automatically.
        val messagesAfterDelete =
            messageDao.getMessagesForConversationOnce("conversation-1")

        assertTrue(messagesAfterDelete.isEmpty())
    }
}
