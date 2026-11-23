package com.smartnotesai.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.smartnotesai.core.database.dao.NoteDao
import com.smartnotesai.core.database.entity.NoteEntity
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var noteDao: NoteDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        noteDao = database.noteDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertNote_andRetrieveNoteById() = runTest {
        val note = NoteEntity(
            id = "note-1",
            title = "Android Testing",
            content = "Learning Room DAO testing",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        noteDao.insertNote(note)

        val result = noteDao.getNoteById("note-1")

        assertEquals(note, result)
    }

    @Test
    fun getAllNotes_returnsInsertedNotes() = runTest {
        val note = NoteEntity(
            id = "note-1",
            title = "First Note",
            content = "First content",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        noteDao.insertNote(note)

        val result = noteDao.getAllNotes().first()

        assertEquals(1, result.size)
        assertEquals(note, result.first())
    }

    @Test
    fun getAllNotes_returnsNotesOrderedByUpdatedAtDescending() = runTest {
        val olderNote = NoteEntity(
            id = "note-1",
            title = "Older Note",
            content = "Older content",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val newerNote = NoteEntity(
            id = "note-2",
            title = "Newer Note",
            content = "Newer content",
            createdAt = 3000L,
            updatedAt = 4000L
        )

        noteDao.insertNote(olderNote)
        noteDao.insertNote(newerNote)

        val result = noteDao.getAllNotes().first()

        assertEquals(2, result.size)
        assertEquals("note-2", result[0].id)
        assertEquals("note-1", result[1].id)
    }

    @Test
    fun deleteNote_removesNoteFromDatabase() = runTest {
        val note = NoteEntity(
            id = "note-1",
            title = "Temporary Note",
            content = "This note will be deleted",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        noteDao.insertNote(note)
        noteDao.deleteNote("note-1")

        val result = noteDao.getNoteById("note-1")

        assertNull(result)
        assertEquals(0, noteDao.getAllNotes().first().size)
    }
}
