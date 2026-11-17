package com.smartnotesai.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnotesai.domain.model.Note
import com.smartnotesai.domain.usecase.CreateNoteUseCase
import com.smartnotesai.domain.usecase.GetNoteByIdUseCase
import com.smartnotesai.domain.usecase.UpdateNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val createNoteUseCase: CreateNoteUseCase,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    fun onAction(action: EditorUiAction) {
        when (action) {
            is EditorUiAction.TitleChanged -> {
                _uiState.update {
                    it.copy(
                        title = action.title,
                        isSaved = false
                    )
                }
            }

            is EditorUiAction.ContentChanged -> {
                _uiState.update {
                    it.copy(
                        content = action.content,
                        isSaved = false
                    )
                }
            }

            is EditorUiAction.LoadNote -> {
                loadNote()
            }

            is EditorUiAction.SaveNote -> {
                saveNote()
            }

            is EditorUiAction.ClearError -> {
                _uiState.update {
                    it.copy(
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun initialize(noteId: String?) {
        if (noteId == null) {
            _uiState.update {
                EditorUiState()
            }
            return
        }

        _uiState.update {
            it.copy(
                noteId = noteId,
                isLoading = true
            )
        }

        loadNote()
    }

    private fun loadNote() {
        val noteId = _uiState.value.noteId ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val note = getNoteByIdUseCase(noteId)
                if (note == null) {
                    _uiState.update {
                        it.copy(
                            errorMessage = "Note not found",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        title = note.title,
                        content = note.content,
                        isLoading = false
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Unable to load note",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun saveNote() {
        val currentState = _uiState.value

        if (currentState.title.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Note title cannot be empty"
                )
            }
            return
        }
        if (currentState.isSaving) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    isSaved = false
                )
            }

            try {
                val now = System.currentTimeMillis()
                val noteId = currentState.noteId

                if (noteId == null) {
                    createNoteUseCase(
                        Note(
                            title = currentState.title.trim(),
                            content = currentState.content,
                            id = UUID.randomUUID().toString(),
                            updatedAt = now,
                            createdAt = now
                        )
                    )
                } else {
                    val existingNote = getNoteByIdUseCase(noteId)
                    if (existingNote == null) {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = "Note not found"
                            )
                        }
                        return@launch
                    }
                    updateNoteUseCase(
                        Note(
                            id = noteId,
                            title = currentState.title.trim(),
                            content = currentState.content,
                            updatedAt = now,
                            createdAt = existingNote.createdAt
                        )
                    )
                }
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSaved = true
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Unable to save note",
                        isSaving = false
                    )
                }
            }
        }
    }
}