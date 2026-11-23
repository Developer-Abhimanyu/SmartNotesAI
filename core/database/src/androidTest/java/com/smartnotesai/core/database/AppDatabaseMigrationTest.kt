package com.smartnotesai.core.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.After
import org.junit.Test

class AppDatabaseMigrationTest {

    private val testDatabaseName = "smartnotesai-migration-test"

    @After
    fun cleanup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(testDatabaseName)
    }

    @Test
    fun migrate1To2_preservesExistingNotesAndCreatesChatTables() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseFile = context.getDatabasePath(testDatabaseName)

        databaseFile.parentFile?.mkdirs()

        // Create the original version 1 database.
        val version1Database = SQLiteDatabase.openOrCreateDatabase(
            databaseFile,
            null
        )

        version1Database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS notes (
                id TEXT NOT NULL,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )

        version1Database.execSQL(
            """
            INSERT INTO notes (id, title, content, createdAt, updatedAt)
            VALUES (
                'note-1',
                'Existing Note',
                'This note must survive migration',
                1000,
                2000
            )
            """.trimIndent()
        )

        version1Database.version = 1
        version1Database.close()

        // Open the version 1 database using the actual Room migration.
        val database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            testDatabaseName
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

        try {
            // Force database opening and migration.
            database.openHelper.writableDatabase

            val migratedNote = database.noteDao().getNoteById("note-1")

            assertNotNull(migratedNote)
            assertEquals("Existing Note", migratedNote?.title)
            assertEquals(
                "This note must survive migration",
                migratedNote?.content
            )

            // Verify new chat DAOs are accessible after migration.
            assertNotNull(database.chatConversationDao())
            assertNotNull(database.chatMessageDao())

        } finally {
            database.close()
        }
    }
}
