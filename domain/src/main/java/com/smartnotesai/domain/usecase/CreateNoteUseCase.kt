package com.smartnotesai.domain.usecase

import com.smartnotesai.domain.model.Note
import com.smartnotesai.domain.repository.NoteRepository
import javax.inject.Inject

class CreateNoteUseCase @Inject constructor(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(note: Note) {
        noteRepository.saveNote(note)
    }
}