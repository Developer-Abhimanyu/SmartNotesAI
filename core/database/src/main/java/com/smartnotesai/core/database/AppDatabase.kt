package com.smartnotesai.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smartnotesai.core.database.dao.NoteDao
import com.smartnotesai.core.database.entity.NoteEntity
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smartnotesai.core.database.dao.ChatConversationDao
import com.smartnotesai.core.database.dao.ChatMessageDao
import com.smartnotesai.core.database.entity.ChatConversationEntity
import com.smartnotesai.core.database.entity.ChatMessageEntity

@Database(
    entities = [
        NoteEntity::class,
        ChatConversationEntity::class,
        ChatMessageEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun chatConversationDao(): ChatConversationDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS chat_conversations (
                        id TEXT NOT NULL,
                        title TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS chat_messages (
                        id TEXT NOT NULL,
                        conversationId TEXT NOT NULL,
                        role TEXT NOT NULL,
                        content TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        PRIMARY KEY(id),
                        FOREIGN KEY(conversationId)
                            REFERENCES chat_conversations(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_chat_messages_conversationId
                    ON chat_messages(conversationId)
                    """.trimIndent()
                )
            }
        }
    }
}
