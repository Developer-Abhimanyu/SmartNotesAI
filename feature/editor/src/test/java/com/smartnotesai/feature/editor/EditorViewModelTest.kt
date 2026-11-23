package com.smartnotesai.feature.editor

import com.smartnotesai.domain.model.AiError
import com.smartnotesai.domain.model.MeetingSummary
import com.smartnotesai.domain.model.Note
import com.smartnotesai.domain.repository.AiRepository
import com.smartnotesai.domain.repository.MeetingSummaryRepository
import com.smartnotesai.domain.repository.NoteRepository
import com.smartnotesai.domain.usecase.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeNoteRepository : NoteRepository {

        val savedNotes = mutableListOf<Note>()
        var noteToReturn: Note? = null

        var getNoteByIdError: Exception? = null
        var saveNoteError: Exception? = null

        override fun getAllNotes(): Flow<List<Note>> =
            flowOf(savedNotes)

        override suspend fun getNoteById(id: String): Note? {
            getNoteByIdError?.let { throw it }
            return noteToReturn?.takeIf { it.id == id }
        }

        override suspend fun saveNote(note: Note) {
            saveNoteError?.let { throw it }

            savedNotes.add(note)
            noteToReturn = note
        }

        override suspend fun deleteNote(id: String) {
            savedNotes.removeAll { it.id == id }
        }
    }

    private class FakeAiRepository : AiRepository {

        var textResponse = "AI response"
        var textError: Exception? = null

        override suspend fun generateText(prompt: String): String {
            textError?.let { throw it }
            return textResponse
        }

        override fun generateTextStream(prompt: String): Flow<String> =
            emptyFlow()

        override fun generateImageUnderstandingStream(
            image: com.smartnotesai.domain.model.ImageInput,
            prompt: String
        ): Flow<String> = emptyFlow()
    }

    private class FakeMeetingSummaryRepository :
        MeetingSummaryRepository {

        var summaryError: Exception? = null

        var summaryResponse = MeetingSummary(
            summary = "Meeting summary",
            decisions = listOf("Use Kotlin"),
            actionItems = emptyList()
        )

        override suspend fun generateMeetingSummary(
            meetingNotes: String
        ): MeetingSummary {
            summaryError?.let { throw it }
            return summaryResponse
        }
    }

    private val noteRepository = FakeNoteRepository()
    private val aiRepository = FakeAiRepository()

    private val meetingSummaryRepository =
        FakeMeetingSummaryRepository()

    private fun createViewModel(): EditorViewModel {

        val generateTextUseCase = GenerateTextUseCase(aiRepository)

        return EditorViewModel(
            createNoteUseCase = CreateNoteUseCase(noteRepository),
            getNoteByIdUseCase = GetNoteByIdUseCase(noteRepository),
            updateNoteUseCase = UpdateNoteUseCase(noteRepository),
            summarizeNoteUseCase = SummarizeNoteUseCase(generateTextUseCase),
            rewriteTextUseCase = RewriteTextUseCase(generateTextUseCase),
            correctGrammarUseCase = CorrectGrammarUseCase(generateTextUseCase),
            generateMeetingSummaryUseCase =
                GenerateMeetingSummaryUseCase(
                    meetingSummaryRepository
                )
        )
    }

    @Test
    fun initialState_containsDefaultValues() {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value

        assertEquals("", state.title)
        assertEquals("", state.content)
        assertNull(state.noteId)
        assertFalse(state.isLoading)
        assertFalse(state.isSaving)
        assertFalse(state.isSaved)
        assertNull(state.errorMessage)
        assertEquals("Professional", state.selectedRewriteStyle)
    }

    @Test
    fun titleChanged_updatesTitleAndMarksNoteUnsaved() {
        val viewModel = createViewModel()

        viewModel.onAction(EditorUiAction.TitleChanged("My Note"))

        val state = viewModel.uiState.value

        assertEquals("My Note", state.title)
        assertFalse(state.isSaved)
    }

    @Test
    fun contentChanged_updatesContentAndMarksNoteUnsaved() {
        val viewModel = createViewModel()

        viewModel.onAction(
            EditorUiAction.ContentChanged("Learning Android Testing")
        )

        val state = viewModel.uiState.value

        assertEquals("Learning Android Testing", state.content)
        assertFalse(state.isSaved)
    }

    @Test
    fun saveNote_withBlankTitle_setsValidationError() {
        val viewModel = createViewModel()

        viewModel.onAction(EditorUiAction.SaveNote)

        assertEquals(
            "Note title cannot be empty",
            viewModel.uiState.value.errorMessage
        )

        assertTrue(noteRepository.savedNotes.isEmpty())
    }

    @Test
    fun clearError_removesErrorMessage() {
        val viewModel = createViewModel()

        viewModel.onAction(EditorUiAction.SaveNote)
        viewModel.onAction(EditorUiAction.ClearError)

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun rewriteStyleChanged_updatesSupportedStyle() {
        val viewModel = createViewModel()

        viewModel.onAction(
            EditorUiAction.RewriteStyleChanged("Friendly")
        )

        assertEquals(
            "Friendly",
            viewModel.uiState.value.selectedRewriteStyle
        )
    }

    @Test
    fun rewriteStyleChanged_ignoresUnsupportedStyle() {
        val viewModel = createViewModel()

        viewModel.onAction(
            EditorUiAction.RewriteStyleChanged("Unsupported")
        )

        assertEquals(
            "Professional",
            viewModel.uiState.value.selectedRewriteStyle
        )
    }

    @Test
    fun clearRewriteResult_removesRewrittenText() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(
            EditorUiAction.ContentChanged("Original text")
        )
        viewModel.onAction(EditorUiAction.RewriteText)

        assertEquals(
            "AI response",
            viewModel.uiState.value.rewrittenText
        )

        viewModel.onAction(EditorUiAction.ClearRewriteResult)

        assertNull(viewModel.uiState.value.rewrittenText)
    }

    @Test
    fun clearCorrectionResult_removesCorrectedText() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(
            EditorUiAction.ContentChanged("Original text")
        )
        viewModel.onAction(EditorUiAction.ProofreadText)

        assertEquals(
            "AI response",
            viewModel.uiState.value.correctedText
        )

        viewModel.onAction(EditorUiAction.ClearCorrectionResult)

        assertNull(viewModel.uiState.value.correctedText)
    }

    @Test
    fun initialize_withExistingNote_loadsNoteSuccessfully() = runTest {
        val existingNote = Note(
            id = "note-1",
            title = "Android Development",
            content = "Learning Jetpack Compose",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        noteRepository.noteToReturn = existingNote

        val viewModel = createViewModel()
        viewModel.initialize("note-1")

        val state = viewModel.uiState.value

        assertEquals("note-1", state.noteId)
        assertEquals("Android Development", state.title)
        assertEquals("Learning Jetpack Compose", state.content)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun initialize_withMissingNote_setsNotFoundError() = runTest {
        val viewModel = createViewModel()

        viewModel.initialize("missing-note")

        val state = viewModel.uiState.value

        assertEquals("Note not found", state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun initialize_whenRepositoryFails_setsLoadingError() = runTest {
        noteRepository.getNoteByIdError =
            IllegalStateException("Database error")

        val viewModel = createViewModel()

        viewModel.initialize("note-1")

        val state = viewModel.uiState.value

        assertEquals("Unable to load note", state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun saveNote_withNewNote_trimsTitleAndSavesSuccessfully() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(
            EditorUiAction.TitleChanged("  My New Note  ")
        )
        viewModel.onAction(
            EditorUiAction.ContentChanged("Learning Kotlin Testing")
        )

        viewModel.onAction(EditorUiAction.SaveNote)

        val savedNote = noteRepository.savedNotes.single()

        assertEquals("My New Note", savedNote.title)
        assertEquals("Learning Kotlin Testing", savedNote.content)
        assertTrue(viewModel.uiState.value.isSaved)
        assertFalse(viewModel.uiState.value.isSaving)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun saveNote_withExistingNote_preservesCreatedAt() = runTest {
        val originalNote = Note(
            id = "note-1",
            title = "Original Title",
            content = "Original Content",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        noteRepository.noteToReturn = originalNote

        val viewModel = createViewModel()

        viewModel.initialize("note-1")

        viewModel.onAction(
            EditorUiAction.TitleChanged("Updated Title")
        )
        viewModel.onAction(
            EditorUiAction.ContentChanged("Updated Content")
        )

        viewModel.onAction(EditorUiAction.SaveNote)

        val updatedNote = noteRepository.savedNotes.last()

        assertEquals("note-1", updatedNote.id)
        assertEquals("Updated Title", updatedNote.title)
        assertEquals("Updated Content", updatedNote.content)
        assertEquals(1000L, updatedNote.createdAt)
        assertTrue(updatedNote.updatedAt >= originalNote.updatedAt)
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun saveNote_whenRepositoryFails_setsSaveError() = runTest {
        noteRepository.saveNoteError =
            IllegalStateException("Database error")

        val viewModel = createViewModel()

        viewModel.onAction(
            EditorUiAction.TitleChanged("My Note")
        )
        viewModel.onAction(
            EditorUiAction.ContentChanged("Some content")
        )

        viewModel.onAction(EditorUiAction.SaveNote)

        val state = viewModel.uiState.value

        assertEquals("Unable to save note", state.errorMessage)
        assertFalse(state.isSaving)
        assertFalse(state.isSaved)
    }

    @Test
    fun summarizeNote_withValidContent_returnsSummary() = runTest {
        aiRepository.textResponse = "This is a concise summary."

        val viewModel = createViewModel()

        viewModel.onAction(
            EditorUiAction.ContentChanged("Android is a mobile operating system.")
        )

        viewModel.onAction(EditorUiAction.SummarizeNote)

        val state = viewModel.uiState.value

        assertEquals("This is a concise summary.", state.summary)
        assertFalse(state.isSummarizing)
        assertNull(state.errorMessage)
    }

    @Test
    fun rewriteText_withValidContent_returnsRewrittenText() = runTest {
        aiRepository.textResponse = "This is professionally rewritten text."

        val viewModel = createViewModel()

        viewModel.onAction(
            EditorUiAction.ContentChanged("original text")
        )

        viewModel.onAction(
            EditorUiAction.RewriteStyleChanged("Professional")
        )

        viewModel.onAction(EditorUiAction.RewriteText)

        val state = viewModel.uiState.value

        assertEquals(
            "This is professionally rewritten text.",
            state.rewrittenText
        )
        assertFalse(state.isRewriting)
        assertNull(state.errorMessage)
    }

    @Test
    fun proofreadText_withValidContent_returnsCorrectedText() = runTest {
        aiRepository.textResponse = "I am learning Android development."

        val viewModel = createViewModel()

        viewModel.onAction(
            EditorUiAction.ContentChanged("i am learning android development")
        )

        viewModel.onAction(EditorUiAction.ProofreadText)

        val state = viewModel.uiState.value

        assertEquals(
            "I am learning Android development.",
            state.correctedText
        )
        assertFalse(state.isProofreading)
        assertNull(state.errorMessage)
    }

    @Test
    fun generateMeetingSummary_withValidNotes_returnsStructuredSummary() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(
            EditorUiAction.ContentChanged(
                "Discussed project architecture and decided to use Kotlin."
            )
        )

        viewModel.onAction(EditorUiAction.GenerateMeetingSummary)

        val state = viewModel.uiState.value

        assertEquals(
            "Meeting summary",
            state.meetingSummary?.summary
        )
        assertEquals(
            listOf("Use Kotlin"),
            state.meetingSummary?.decisions
        )
        assertFalse(state.isGeneratingMeetingSummary)
        assertNull(state.errorMessage)
    }

    @Test
    fun summarizeNote_withEmptyContent_setsValidationError() {
        val viewModel = createViewModel()

        viewModel.onAction(EditorUiAction.SummarizeNote)

        assertEquals(
            "Add some note content before summarizing",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun rewriteText_withEmptyContent_setsValidationError() {
        val viewModel = createViewModel()

        viewModel.onAction(EditorUiAction.RewriteText)

        assertEquals(
            "Add some note content before rewriting",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun proofreadText_withEmptyContent_setsValidationError() {
        val viewModel = createViewModel()

        viewModel.onAction(EditorUiAction.ProofreadText)

        assertEquals(
            "Add some note content before proofreading",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun generateMeetingSummary_withEmptyContent_setsValidationError() {
        val viewModel = createViewModel()

        viewModel.onAction(EditorUiAction.GenerateMeetingSummary)

        assertEquals(
            "Meeting notes cannot be empty",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun summarizeNote_whenAiFails_setsFallbackError() = runTest {
        aiRepository.textError =
            IllegalStateException("AI service unavailable")

        val viewModel = createViewModel()

        viewModel.onAction(
            EditorUiAction.ContentChanged("Some valid note content")
        )

        viewModel.onAction(EditorUiAction.SummarizeNote)

        val state = viewModel.uiState.value

        assertEquals(
            "Unable to summarize note. Please try again.",
            state.errorMessage
        )
        assertFalse(state.isSummarizing)
    }

    @Test
    fun generateMeetingSummary_whenAiFails_setsFallbackError() = runTest {
        meetingSummaryRepository.summaryError =
            IllegalStateException("AI service unavailable")

        val viewModel = createViewModel()

        viewModel.onAction(
            EditorUiAction.ContentChanged("Some meeting notes")
        )

        viewModel.onAction(EditorUiAction.GenerateMeetingSummary)

        val state = viewModel.uiState.value

        assertEquals(
            "Unable to generate meeting summary. Please try again.",
            state.errorMessage
        )
        assertFalse(state.isGeneratingMeetingSummary)
    }

    @Test
    fun summarizeNote_whenNetworkError_setsFriendlyMessage() = runTest {
        aiRepository.textError = AiError.Network()

        val viewModel = createViewModel()
        viewModel.onAction(
            EditorUiAction.ContentChanged("Some valid note content")
        )
        viewModel.onAction(EditorUiAction.SummarizeNote)

        assertEquals(
            "Check your internet connection and try again.",
            viewModel.uiState.value.errorMessage
        )
        assertFalse(viewModel.uiState.value.isSummarizing)
    }

    @Test
    fun summarizeNote_whenRateLimitError_setsFriendlyMessage() = runTest {
        aiRepository.textError = AiError.RateLimit()

        val viewModel = createViewModel()
        viewModel.onAction(
            EditorUiAction.ContentChanged("Some valid note content")
        )
        viewModel.onAction(EditorUiAction.SummarizeNote)

        assertEquals(
            "AI service limit reached. Please try later.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun summarizeNote_whenTimeoutError_setsFriendlyMessage() = runTest {
        aiRepository.textError = AiError.Timeout()

        val viewModel = createViewModel()
        viewModel.onAction(
            EditorUiAction.ContentChanged("Some valid note content")
        )
        viewModel.onAction(EditorUiAction.SummarizeNote)

        assertEquals(
            "The AI request timed out. Please try again.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun summarizeNote_whenSafetyBlocked_setsFriendlyMessage() = runTest {
        aiRepository.textError = AiError.SafetyBlocked()

        val viewModel = createViewModel()
        viewModel.onAction(
            EditorUiAction.ContentChanged("Some valid note content")
        )
        viewModel.onAction(EditorUiAction.SummarizeNote)

        assertEquals(
            "This request could not be processed.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun summarizeNote_whenConfigurationError_setsFriendlyMessage() = runTest {
        aiRepository.textError = AiError.Configuration()

        val viewModel = createViewModel()
        viewModel.onAction(
            EditorUiAction.ContentChanged("Some valid note content")
        )
        viewModel.onAction(EditorUiAction.SummarizeNote)

        assertEquals(
            "AI service is not configured correctly.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun summarizeNote_whenServerError_setsFriendlyMessage() = runTest {
        aiRepository.textError = AiError.Server()

        val viewModel = createViewModel()
        viewModel.onAction(
            EditorUiAction.ContentChanged("Some valid note content")
        )
        viewModel.onAction(EditorUiAction.SummarizeNote)

        assertEquals(
            "AI service is temporarily unavailable.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun summarizeNote_whenUnknownError_setsFriendlyMessage() = runTest {
        aiRepository.textError = AiError.Unknown()

        val viewModel = createViewModel()
        viewModel.onAction(
            EditorUiAction.ContentChanged("Some valid note content")
        )
        viewModel.onAction(EditorUiAction.SummarizeNote)

        assertEquals(
            "Something went wrong. Please try again.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun summarizeNote_whenCancelled_doesNotSetErrorMessage() = runTest {
        aiRepository.textError = CancellationException("Request cancelled")

        val viewModel = createViewModel()

        viewModel.onAction(
            EditorUiAction.ContentChanged("Some valid note content")
        )

        viewModel.onAction(EditorUiAction.SummarizeNote)

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun rewriteText_whenCancelled_doesNotSetErrorMessage() = runTest {
        aiRepository.textError = CancellationException("Request cancelled")

        val viewModel = createViewModel()

        viewModel.onAction(
            EditorUiAction.ContentChanged("Some valid note content")
        )

        viewModel.onAction(EditorUiAction.RewriteText)

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun proofreadText_whenCancelled_doesNotSetErrorMessage() = runTest {
        aiRepository.textError = CancellationException("Request cancelled")

        val viewModel = createViewModel()

        viewModel.onAction(
            EditorUiAction.ContentChanged("Some valid note content")
        )

        viewModel.onAction(EditorUiAction.ProofreadText)

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun generateMeetingSummary_whenCancelled_doesNotSetErrorMessage() = runTest {
        meetingSummaryRepository.summaryError =
            CancellationException("Request cancelled")

        val viewModel = createViewModel()

        viewModel.onAction(
            EditorUiAction.ContentChanged("Some meeting notes")
        )

        viewModel.onAction(EditorUiAction.GenerateMeetingSummary)

        assertNull(viewModel.uiState.value.errorMessage)
    }
}
