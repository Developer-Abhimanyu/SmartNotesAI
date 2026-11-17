package com.smartnotesai.data.repository

import com.smartnotesai.core.database.dao.NoteDao
import com.smartnotesai.data.mapper.toDomain
import com.smartnotesai.data.mapper.toEntity
import com.smartnotesai.domain.model.Note
import com.smartnotesai.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(private val noteDao: NoteDao) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { notes ->
            notes.map { it.toDomain() }
        }
    }

    override suspend fun getNoteById(id: String): Note? {
        return noteDao.getNoteById(id)?.toDomain()
    }

    override suspend fun saveNote(note: Note) {
        noteDao.insertNote(note.toEntity())
    }

    override suspend fun deleteNote(id: String) {
        noteDao.deleteNote(id)
    }
}