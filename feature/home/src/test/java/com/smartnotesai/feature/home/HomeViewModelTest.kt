package com.smartnotesai.feature.home

import com.smartnotesai.domain.model.Note
import com.smartnotesai.domain.repository.NoteRepository
import com.smartnotesai.domain.usecase.CreateNoteUseCase
import com.smartnotesai.domain.usecase.DeleteNoteUseCase
import com.smartnotesai.domain.usecase.GetAllNotesUseCase
import com.smartnotesai.domain.usecase.GetNoteByIdUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeNoteRepository : NoteRepository {

        val notes = MutableStateFlow<List<Note>>(emptyList())

        var getAllNotesError: Exception? = null
        var getNoteByIdError: Exception? = null
        var saveNoteError: Exception? = null
        var deleteNoteError: Exception? = null

        override fun getAllNotes(): Flow<List<Note>> {
            val error = getAllNotesError

            return if (error != null) {
                flow { throw error }
            } else {
                notes
            }
        }

        override suspend fun getNoteById(id: String): Note? {
            getNoteByIdError?.let { throw it }
            return notes.value.find { it.id == id }
        }

        override suspend fun saveNote(note: Note) {
            saveNoteError?.let { throw it }
            notes.value = notes.value + note
        }

        override suspend fun deleteNote(id: String) {
            deleteNoteError?.let { throw it }
            notes.value = notes.value.filterNot { it.id == id }
        }
    }

    private val noteRepository = FakeNoteRepository()

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            getAllNotesUseCase = GetAllNotesUseCase(noteRepository),
            createNoteUseCase = CreateNoteUseCase(noteRepository),
            deleteNoteUseCase = DeleteNoteUseCase(noteRepository),
            getNoteByIdUseCase = GetNoteByIdUseCase(noteRepository)
        )
    }

    private fun createNote(
        id: String,
        title: String,
        content: String,
        updatedAt: Long
    ) = Note(
        id = id,
        title = title,
        content = content,
        createdAt = updatedAt - 100,
        updatedAt = updatedAt
    )

    @Test
    fun initialLoad_loadsNotesSuccessfully() {
        val firstNote = createNote(
            "1", "Android", "Learning Kotlin", 1000L
        )
        val secondNote = createNote(
            "2", "Compose", "Building UI", 2000L
        )

        noteRepository.notes.value = listOf(firstNote, secondNote)

        val viewModel = createViewModel()

        assertEquals(2, viewModel.uiState.value.notes.size)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun initialLoad_whenRepositoryFails_setsErrorMessage() {
        noteRepository.getAllNotesError =
            IllegalStateException("Database unavailable")

        val viewModel = createViewModel()

        assertEquals(
            "Database unavailable",
            viewModel.uiState.value.errorMessage
        )
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun addNote_createsUntitledNote() {
        val viewModel = createViewModel()

        viewModel.onAction(HomeUiAction.AddNote)

        val addedNote = noteRepository.notes.value.single()

        assertTrue(addedNote.id.isNotBlank())
        assertEquals("Untitled Note", addedNote.title)
        assertEquals("", addedNote.content)
        assertTrue(addedNote.createdAt > 0)
        assertEquals(addedNote.createdAt, addedNote.updatedAt)
    }

    @Test
    fun addNote_whenRepositoryFails_setsErrorMessage() {
        noteRepository.saveNoteError =
            IllegalStateException("Unable to save")

        val viewModel = createViewModel()

        viewModel.onAction(HomeUiAction.AddNote)

        assertEquals(
            "Unable to save",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun deleteNote_removesNoteSuccessfully() {
        val note = createNote("1", "Android", "Kotlin", 1000L)
        noteRepository.notes.value = listOf(note)

        val viewModel = createViewModel()

        viewModel.onAction(HomeUiAction.DeleteNote("1"))

        assertTrue(noteRepository.notes.value.isEmpty())
        assertTrue(viewModel.uiState.value.notes.isEmpty())
    }

    @Test
    fun deleteNote_whenRepositoryFails_setsErrorMessage() {
        noteRepository.deleteNoteError =
            IllegalStateException("Delete failed")

        val viewModel = createViewModel()

        viewModel.onAction(HomeUiAction.DeleteNote("1"))

        assertEquals(
            "Delete failed",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun openNote_withExistingNote_selectsNote() {
        val note = createNote("1", "Android", "Kotlin", 1000L)
        noteRepository.notes.value = listOf(note)

        val viewModel = createViewModel()

        viewModel.onAction(HomeUiAction.OpenNote("1"))

        assertEquals("1", viewModel.uiState.value.selectedNoteId)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun openNote_withMissingNote_setsNotFoundError() {
        val viewModel = createViewModel()

        viewModel.onAction(HomeUiAction.OpenNote("missing"))

        assertEquals(
            "Note not found",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun openNote_whenRepositoryFails_setsErrorMessage() {
        noteRepository.getNoteByIdError =
            IllegalStateException("Database error")

        val viewModel = createViewModel()

        viewModel.onAction(HomeUiAction.OpenNote("1"))

        assertEquals(
            "Database error",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun searchQueryChanged_filtersNotesByTitleIgnoringCase() {
        val androidNote = createNote(
            "1", "Android Development", "Kotlin", 1000L
        )
        val cookingNote = createNote(
            "2", "Cooking", "Recipes", 2000L
        )

        noteRepository.notes.value = listOf(androidNote, cookingNote)

        val viewModel = createViewModel()

        viewModel.onAction(
            HomeUiAction.SearchQueryChanged("ANDROID")
        )

        assertEquals(
            listOf(androidNote),
            viewModel.filteredNotes
        )
    }

    @Test
    fun searchQueryChanged_filtersNotesByContent() {
        val androidNote = createNote(
            "1", "My Notes", "Learning Jetpack Compose", 1000L
        )
        val cookingNote = createNote(
            "2", "Recipes", "Making dosa", 2000L
        )

        noteRepository.notes.value = listOf(androidNote, cookingNote)

        val viewModel = createViewModel()

        viewModel.onAction(
            HomeUiAction.SearchQueryChanged("JETPACK")
        )

        assertEquals(
            listOf(androidNote),
            viewModel.filteredNotes
        )
    }

    @Test
    fun sortOrderChanged_sortsRecentlyUpdatedFirst() {
        val oldest = createNote("1", "Alpha", "", 1000L)
        val newest = createNote("2", "Beta", "", 3000L)
        val middle = createNote("3", "Gamma", "", 2000L)

        noteRepository.notes.value = listOf(oldest, newest, middle)

        val viewModel = createViewModel()

        viewModel.onAction(
            HomeUiAction.SortOrderChanged(
                NoteSortOrder.RECENTLY_UPDATED
            )
        )

        assertEquals(
            listOf(newest, middle, oldest),
            viewModel.filteredNotes
        )
    }

    @Test
    fun sortOrderChanged_sortsOldestUpdatedFirst() {
        val oldest = createNote("1", "Alpha", "", 1000L)
        val newest = createNote("2", "Beta", "", 3000L)
        val middle = createNote("3", "Gamma", "", 2000L)

        noteRepository.notes.value = listOf(oldest, newest, middle)

        val viewModel = createViewModel()

        viewModel.onAction(
            HomeUiAction.SortOrderChanged(
                NoteSortOrder.OLDEST_UPDATED
            )
        )

        assertEquals(
            listOf(oldest, middle, newest),
            viewModel.filteredNotes
        )
    }

    @Test
    fun sortOrderChanged_sortsAlphabetically() {
        val beta = createNote("1", "Beta", "", 1000L)
        val alpha = createNote("2", "Alpha", "", 2000L)
        val gamma = createNote("3", "Gamma", "", 3000L)

        noteRepository.notes.value = listOf(beta, alpha, gamma)

        val viewModel = createViewModel()

        viewModel.onAction(
            HomeUiAction.SortOrderChanged(
                NoteSortOrder.ALPHABETICAL
            )
        )

        assertEquals(
            listOf(alpha, beta, gamma),
            viewModel.filteredNotes
        )
    }

    @Test
    fun loadNotes_action_refreshesNotes() {
        val firstNote = createNote("1", "Android", "Kotlin", 1000L)
        noteRepository.notes.value = listOf(firstNote)

        val viewModel = createViewModel()

        val secondNote = createNote("2", "Compose", "Jetpack", 2000L)
        noteRepository.notes.value = listOf(firstNote, secondNote)

        viewModel.onAction(HomeUiAction.LoadNotes)

        assertEquals(2, viewModel.uiState.value.notes.size)
        assertTrue(viewModel.uiState.value.notes.contains(secondNote))
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun notesFlow_updatesUiStateWhenRepositoryChanges() {
        val firstNote = createNote("1", "Android", "Kotlin", 1000L)
        noteRepository.notes.value = listOf(firstNote)

        val viewModel = createViewModel()

        assertEquals(1, viewModel.uiState.value.notes.size)

        val secondNote = createNote("2", "Compose", "Jetpack", 2000L)

        noteRepository.notes.value = listOf(firstNote, secondNote)

        assertEquals(2, viewModel.uiState.value.notes.size)
        assertEquals(
            listOf(firstNote, secondNote),
            viewModel.uiState.value.notes
        )
    }

    @Test
    fun searchQueryChanged_withWhitespace_returnsAllNotes() {
        val firstNote = createNote("1", "Android", "Kotlin", 1000L)
        val secondNote = createNote("2", "Compose", "Jetpack", 2000L)

        noteRepository.notes.value = listOf(firstNote, secondNote)

        val viewModel = createViewModel()

        viewModel.onAction(
            HomeUiAction.SearchQueryChanged("   ")
        )

        assertEquals(2, viewModel.filteredNotes.size)
    }

    @Test
    fun searchQueryChanged_combinedWithSorting_returnsSortedMatches() {
        val androidOld = createNote(
            "1", "Android Basics", "Kotlin", 1000L
        )
        val androidNew = createNote(
            "2", "Android Advanced", "Compose", 3000L
        )
        val unrelated = createNote(
            "3", "Cooking", "Recipes", 2000L
        )

        noteRepository.notes.value =
            listOf(androidOld, unrelated, androidNew)

        val viewModel = createViewModel()

        viewModel.onAction(
            HomeUiAction.SearchQueryChanged("Android")
        )

        viewModel.onAction(
            HomeUiAction.SortOrderChanged(
                NoteSortOrder.OLDEST_UPDATED
            )
        )

        assertEquals(
            listOf(androidOld, androidNew),
            viewModel.filteredNotes
        )
    }

    @Test
    fun addNote_whenCancelled_doesNotSetErrorMessage() {
        noteRepository.saveNoteError =
            CancellationException("Add cancelled")

        val viewModel = createViewModel()

        viewModel.onAction(HomeUiAction.AddNote)

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun deleteNote_whenCancelled_doesNotSetErrorMessage() {
        noteRepository.deleteNoteError =
            CancellationException("Delete cancelled")

        val viewModel = createViewModel()

        viewModel.onAction(HomeUiAction.DeleteNote("1"))

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun openNote_whenCancelled_doesNotSetErrorMessage() {
        noteRepository.getNoteByIdError =
            CancellationException("Open cancelled")

        val viewModel = createViewModel()

        viewModel.onAction(HomeUiAction.OpenNote("1"))

        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.selectedNoteId)
    }


}
