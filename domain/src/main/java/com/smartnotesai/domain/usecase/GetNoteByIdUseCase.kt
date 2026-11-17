package com.smartnotesai.domain.usecase

import com.smartnotesai.domain.model.Note
import com.smartnotesai.domain.repository.NoteRepository
import javax.inject.Inject

class GetNoteByIdUseCase @Inject constructor(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(id: String): Note? {
        return noteRepository.getNoteById(id)
    }
}