package com.smartnotesai.domain.usecase

import com.smartnotesai.domain.repository.NoteRepository
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(id: String) {
        noteRepository.deleteNote(id)
    }
}