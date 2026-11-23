package com.smartnotesai.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.smartnotesai.core.database.dao.ChatConversationDao
import com.smartnotesai.core.database.entity.ChatConversationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatConversationDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var conversationDao: ChatConversationDao

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
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertConversation_andRetrieveById() = runTest {
        val conversation = ChatConversationEntity(
            id = "conversation-1",
            title = "Android Discussion",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        conversationDao.insertConversation(conversation)

        val result = conversationDao.getConversationById("conversation-1")

        assertEquals(conversation, result)
    }

    @Test
    fun getAllConversations_returnsConversationsOrderedByUpdatedAtDescending() =
        runTest {
            val older = ChatConversationEntity(
                id = "conversation-1",
                title = "Older Conversation",
                createdAt = 1000L,
                updatedAt = 2000L
            )

            val newer = ChatConversationEntity(
                id = "conversation-2",
                title = "Newer Conversation",
                createdAt = 3000L,
                updatedAt = 4000L
            )

            conversationDao.insertConversation(older)
            conversationDao.insertConversation(newer)

            val result = conversationDao.getAllConversations().first()

            assertEquals(2, result.size)
            assertEquals("conversation-2", result[0].id)
            assertEquals("conversation-1", result[1].id)
        }

    @Test
    fun updateConversation_changesStoredConversation() = runTest {
        val original = ChatConversationEntity(
            id = "conversation-1",
            title = "Original Title",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        conversationDao.insertConversation(original)

        val updated = original.copy(
            title = "Updated Title",
            updatedAt = 3000L
        )

        conversationDao.updateConversation(updated)

        val result = conversationDao.getConversationById("conversation-1")

        assertEquals(updated, result)
    }

    @Test
    fun deleteConversation_removesConversation() = runTest {
        val conversation = ChatConversationEntity(
            id = "conversation-1",
            title = "Temporary Conversation",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        conversationDao.insertConversation(conversation)
        conversationDao.deleteConversation("conversation-1")

        val result = conversationDao.getConversationById("conversation-1")

        assertNull(result)
        assertEquals(0, conversationDao.getAllConversations().first().size)
    }
}
