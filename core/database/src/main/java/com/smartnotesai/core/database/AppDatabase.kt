package com.smartnotesai.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smartnotesai.core.database.dao.NoteDao
import com.smartnotesai.core.database.entity.NoteEntity

@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}