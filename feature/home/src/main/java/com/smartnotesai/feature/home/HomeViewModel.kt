package com.smartnotesai.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnotesai.domain.model.Note
import com.smartnotesai.domain.usecase.CreateNoteUseCase
import com.smartnotesai.domain.usecase.DeleteNoteUseCase
import com.smartnotesai.domain.usecase.GetAllNotesUseCase
import com.smartnotesai.domain.usecase.GetNoteByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val createNoteUseCase: CreateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val getNoteByIdUseCase: GetNoteByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val filteredNotes: List<Note>
        get() {
            val currentState = uiState.value
            val query = currentState.searchQuery.trim()

            val matchingNotes = if (query.isBlank()) {
                currentState.notes
            } else {
                currentState.notes.filter { note ->
                    note.title.contains(query, ignoreCase = true) ||
                            note.content.contains(query, ignoreCase = true)
                }
            }

            return when (currentState.sortOrder) {
                NoteSortOrder.RECENTLY_UPDATED ->
                    matchingNotes.sortedByDescending { it.updatedAt }

                NoteSortOrder.OLDEST_UPDATED ->
                    matchingNotes.sortedBy { it.updatedAt }

                NoteSortOrder.ALPHABETICAL ->
                    matchingNotes.sortedBy { it.title.lowercase() }
            }
        }

    private var loadNotesJob: Job? = null

    init {
        loadNotes()
    }

    fun onAction(action: HomeUiAction) {
        when (action) {
            is HomeUiAction.LoadNotes -> loadNotes()
            is HomeUiAction.AddNote -> addNote()
            is HomeUiAction.OpenNote -> openNote(action.noteId)
            is HomeUiAction.DeleteNote -> deleteNote(action.noteId)
            is HomeUiAction.SearchQueryChanged -> _uiState.update { it.copy(searchQuery = action.query) }
            is HomeUiAction.SortOrderChanged -> {
                _uiState.update {
                    it.copy(sortOrder = action.sortOrder)
                }
            }
        }
    }

    private fun loadNotes() {
        loadNotesJob?.cancel()
        loadNotesJob = null
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        loadNotesJob = viewModelScope.launch {
            getAllNotesUseCase()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Unable to load notes"
                        )
                    }
                }
                .collect { notes ->
                    _uiState.update {
                        it.copy(
                            notes = notes,
                            errorMessage = null,
                            isLoading = false
                        )
                    }

                }
        }
    }

    private fun deleteNote(noteId: String) {
        viewModelScope.launch {
            try {
                deleteNoteUseCase(noteId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = exception.message ?: "Unable to delete note"
                    )
                }
            }
        }
    }

    private fun addNote() {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()

            val note = Note(
                id = UUID.randomUUID().toString(),
                title = "Untitled Note",
                content = "",
                createdAt = currentTime,
                updatedAt = currentTime
            )

            try {
                createNoteUseCase(note)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = exception.message ?: "Unable to add note"
                    )
                }
            }
        }
    }

    private fun openNote(noteId: String) {
        viewModelScope.launch {
            try {
                val note = getNoteByIdUseCase(noteId)

                if (note != null) {
                    _uiState.update {
                        it.copy(
                            selectedNoteId = note.id,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            errorMessage = "Note not found"
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = exception.message ?: "Unable to open note"
                    )
                }
            }
        }
    }
}