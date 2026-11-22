package com.smartnotesai.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnotesai.domain.model.AiError
import com.smartnotesai.domain.model.Note
import com.smartnotesai.domain.usecase.CorrectGrammarUseCase
import com.smartnotesai.domain.usecase.CreateNoteUseCase
import com.smartnotesai.domain.usecase.GenerateMeetingSummaryUseCase
import com.smartnotesai.domain.usecase.GetNoteByIdUseCase
import com.smartnotesai.domain.usecase.RewriteTextUseCase
import com.smartnotesai.domain.usecase.SummarizeNoteUseCase
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
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val summarizeNoteUseCase: SummarizeNoteUseCase,
    private val rewriteTextUseCase: RewriteTextUseCase,
    private val correctGrammarUseCase: CorrectGrammarUseCase,
    private val generateMeetingSummaryUseCase: GenerateMeetingSummaryUseCase
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

            is EditorUiAction.SummarizeNote -> {
                summarizeNote()
            }

            is EditorUiAction.RewriteText -> {
                rewriteText()
            }

            is EditorUiAction.ClearRewriteResult -> {
                _uiState.update {
                    it.copy(rewrittenText = null)
                }
            }

            is EditorUiAction.RewriteStyleChanged -> {
                if (action.style in RewriteTextUseCase.SUPPORTED_STYLES) {
                    _uiState.update {
                        it.copy(
                            selectedRewriteStyle = action.style,
                            rewrittenText = null,
                            errorMessage = null
                        )
                    }
                }
            }

            is EditorUiAction.ProofreadText -> {
                proofreadText()
            }

            is EditorUiAction.ClearCorrectionResult -> {
                _uiState.update {
                    it.copy(correctedText = null)
                }
            }

            is EditorUiAction.GenerateMeetingSummary -> {
                generateMeetingSummary()
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

    private fun summarizeNote() {
        val content = _uiState.value.content

        if (content.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Add some note content before summarizing")
            }
            return
        }

        if (_uiState.value.isSummarizing) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSummarizing = true,
                    summary = null,
                    errorMessage = null
                )
            }

            try {
                val result = summarizeNoteUseCase(content)

                _uiState.update {
                    it.copy(
                        summary = result,
                        isSummarizing = false
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val message = (exception as? AiError)?.toUserMessage()
                    ?: "Unable to summarize note. Please try again."

                _uiState.update {
                    it.copy(
                        isSummarizing = false,
                        errorMessage = message
                    )
                }
            }
        }
    }

    private fun rewriteText() {
        val currentState = _uiState.value

        if (currentState.content.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Add some note content before rewriting"
                )
            }
            return
        }

        if (currentState.isRewriting) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRewriting = true,
                    rewrittenText = null,
                    errorMessage = null
                )
            }

            try {
                val result = rewriteTextUseCase(
                    originalText = currentState.content,
                    style = currentState.selectedRewriteStyle
                )

                _uiState.update {
                    it.copy(
                        rewrittenText = result,
                        isRewriting = false
                    )
                }

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                val message = (exception as? AiError)?.toUserMessage()
                    ?: "Unable to rewrite text. Please try again."

                _uiState.update {
                    it.copy(
                        isRewriting = false,
                        errorMessage = message
                    )
                }
            }
        }
    }

    private fun proofreadText() {
        val currentState = _uiState.value

        if (currentState.content.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Add some note content before proofreading"
                )
            }
            return
        }

        if (currentState.isProofreading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProofreading = true,
                    correctedText = null,
                    errorMessage = null
                )
            }

            try {
                val result = correctGrammarUseCase(
                    originalText = currentState.content
                )

                _uiState.update {
                    it.copy(
                        correctedText = result,
                        isProofreading = false
                    )
                }

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                val message = (exception as? AiError)?.toUserMessage()
                    ?: "Unable to correct grammar. Please try again."

                _uiState.update {
                    it.copy(
                        isProofreading = false,
                        errorMessage = message
                    )
                }
            }
        }
    }


    private fun generateMeetingSummary() {
        val notes = _uiState.value.content

        if (notes.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Meeting notes cannot be empty")
            }
            return
        }

        if (_uiState.value.isGeneratingMeetingSummary) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGeneratingMeetingSummary = true,
                    meetingSummary = null,
                    errorMessage = null
                )
            }

            try {
                val result = generateMeetingSummaryUseCase(notes)

                _uiState.update {
                    it.copy(
                        meetingSummary = result,
                        isGeneratingMeetingSummary = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (exception: Exception) {
                val message = (exception as? AiError)?.toUserMessage()
                    ?: "Unable to generate meeting summary. Please try again."

                _uiState.update {
                    it.copy(
                        isGeneratingMeetingSummary = false,
                        errorMessage = message
                    )
                }
            }
        }
    }
}