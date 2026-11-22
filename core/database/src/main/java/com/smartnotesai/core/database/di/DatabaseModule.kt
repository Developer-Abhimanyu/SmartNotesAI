package com.smartnotesai.core.database.di

import android.content.Context
import androidx.room.Room
import com.smartnotesai.core.database.AppDatabase
import com.smartnotesai.core.database.dao.ChatConversationDao
import com.smartnotesai.core.database.dao.ChatMessageDao
import com.smartnotesai.core.database.dao.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "smartnotes_ai.db"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideNoteDao(
        database: AppDatabase
    ): NoteDao {
        return database.noteDao()
    }

    @Provides
    fun provideChatConversationDao(
        database: AppDatabase
    ): ChatConversationDao {
        return database.chatConversationDao()
    }

    @Provides
    fun provideChatMessageDao(
        database: AppDatabase
    ): ChatMessageDao {
        return database.chatMessageDao()
    }
}
