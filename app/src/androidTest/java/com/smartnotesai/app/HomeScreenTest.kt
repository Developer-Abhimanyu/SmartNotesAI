package com.smartnotesai.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import com.smartnotesai.domain.model.Note
import com.smartnotesai.domain.repository.NoteRepository
import com.smartnotesai.domain.usecase.CreateNoteUseCase
import com.smartnotesai.domain.usecase.DeleteNoteUseCase
import com.smartnotesai.domain.usecase.GetAllNotesUseCase
import com.smartnotesai.domain.usecase.GetNoteByIdUseCase
import com.smartnotesai.feature.home.HomeScreen
import com.smartnotesai.feature.home.HomeViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testNotes = listOf(
        Note(
            id = "1",
            title = "Android Development",
            content = "Learning Jetpack Compose",
            createdAt = 1000L,
            updatedAt = 2000L
        ),
        Note(
            id = "2",
            title = "Shopping List",
            content = "Milk and vegetables",
            createdAt = 3000L,
            updatedAt = 4000L
        )
    )

    private fun createViewModel(): HomeViewModel {
        val repository = FakeNoteRepository(testNotes)

        return HomeViewModel(
            getAllNotesUseCase = GetAllNotesUseCase(repository),
            createNoteUseCase = CreateNoteUseCase(repository),
            deleteNoteUseCase = DeleteNoteUseCase(repository),
            getNoteByIdUseCase = GetNoteByIdUseCase(repository)
        )
    }

    @Test
    fun homeScreen_displaysInitialContentAndButtons() {
        val viewModel = createViewModel()
        var addNoteClicked = false
        var chatClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                HomeScreen(
                    onAddNoteClick = { addNoteClicked = true },
                    onNoteClick = {},
                    onChatClick = { chatClicked = true },
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithText("SmartNotesAI").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 notes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search your notes...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sort: Recently Updated").assertIsDisplayed()

        composeTestRule.onNodeWithText("Add Note")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithText("Chat with AI")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.runOnIdle {
            assertTrue(addNoteClicked)
            assertTrue(chatClicked)
        }
    }

    @Test
    fun homeScreen_displaysSavedNotes() {
        composeTestRule.setContent {
            MaterialTheme {
                HomeScreen(
                    onAddNoteClick = {},
                    onNoteClick = {},
                    onChatClick = {},
                    viewModel = createViewModel()
                )
            }
        }

        composeTestRule.onNodeWithText("Android Development").assertIsDisplayed()
        composeTestRule.onNodeWithText("Learning Jetpack Compose").assertIsDisplayed()
        composeTestRule.onNodeWithText("Shopping List").assertIsDisplayed()
        composeTestRule.onNodeWithText("Milk and vegetables").assertIsDisplayed()
    }

    @Test
    fun homeScreen_searchFiltersNotesByTitle() {
        composeTestRule.setContent {
            MaterialTheme {
                HomeScreen(
                    onAddNoteClick = {},
                    onNoteClick = {},
                    onChatClick = {},
                    viewModel = createViewModel()
                )
            }
        }

        composeTestRule.onNodeWithText("Search your notes...")
            .performTextInput("Android")

        composeTestRule.onNodeWithText("Android Development").assertIsDisplayed()
        composeTestRule.onNodeWithText("Shopping List").assertDoesNotExist()
    }

    @Test
    fun homeScreen_searchFiltersNotesByContent() {
        composeTestRule.setContent {
            MaterialTheme {
                HomeScreen(
                    onAddNoteClick = {},
                    onNoteClick = {},
                    onChatClick = {},
                    viewModel = createViewModel()
                )
            }
        }

        composeTestRule.onNodeWithText("Search your notes...")
            .performTextInput("vegetables")

        composeTestRule.onNodeWithText("Shopping List").assertIsDisplayed()
        composeTestRule.onNodeWithText("Android Development").assertDoesNotExist()
    }

    @Test
    fun homeScreen_sortSelectorChangesSortOrder() {
        composeTestRule.setContent {
            MaterialTheme {
                HomeScreen(
                    onAddNoteClick = {},
                    onNoteClick = {},
                    onChatClick = {},
                    viewModel = createViewModel()
                )
            }
        }

        composeTestRule.onNodeWithText("Sort: Recently Updated").performClick()
        composeTestRule.onNodeWithText("Oldest Updated").performClick()
        composeTestRule.onNodeWithText("Sort: Oldest Updated").assertIsDisplayed()

        composeTestRule.onNodeWithText("Sort: Oldest Updated").performClick()
        composeTestRule.onNodeWithText("Alphabetical (A-Z)").performClick()
        composeTestRule.onNodeWithText("Sort: Alphabetical (A-Z)").assertIsDisplayed()
    }

    @Test
    fun homeScreen_clickingNoteInvokesCallbackWithNoteId() {
        var selectedNoteId: String? = null

        composeTestRule.setContent {
            MaterialTheme {
                HomeScreen(
                    onAddNoteClick = {},
                    onNoteClick = { selectedNoteId = it },
                    onChatClick = {},
                    viewModel = createViewModel()
                )
            }
        }

        composeTestRule.onNodeWithText("Android Development").performClick()

        composeTestRule.runOnIdle {
            assertEquals("1", selectedNoteId)
        }
    }

    @Test
    fun homeScreen_deleteNoteShowsConfirmationDialog() {
        composeTestRule.setContent {
            MaterialTheme {
                HomeScreen(
                    onAddNoteClick = {},
                    onNoteClick = {},
                    onChatClick = {},
                    viewModel = createViewModel()
                )
            }
        }

        // Select the first note's delete button from the two matching buttons.
        composeTestRule
            .onAllNodesWithContentDescription("Delete Note")
            .get(1)
            .performClick()

        composeTestRule.onNodeWithText("Delete Note?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.onNodeWithText("Delete Note?").assertDoesNotExist()
        composeTestRule.onNodeWithText("Android Development").assertIsDisplayed()
    }

    @Test
    fun homeScreen_deleteConfirmationDeletesNote() {
        composeTestRule.setContent {
            MaterialTheme {
                HomeScreen(
                    onAddNoteClick = {},
                    onNoteClick = {},
                    onChatClick = {},
                    viewModel = createViewModel()
                )
            }
        }

        // Select the first note's delete button from the two matching buttons.
        composeTestRule
            .onAllNodesWithContentDescription("Delete Note")
            .get(1)
            .performClick()

        composeTestRule.onNodeWithText("Delete Note?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Android Development").assertDoesNotExist()
        composeTestRule.onNodeWithText("Shopping List").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 notes").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysEmptyStateWhenNoNotesExist() {
        val emptyRepository = FakeNoteRepository(emptyList())
        val viewModel = HomeViewModel(
            getAllNotesUseCase = GetAllNotesUseCase(emptyRepository),
            createNoteUseCase = CreateNoteUseCase(emptyRepository),
            deleteNoteUseCase = DeleteNoteUseCase(emptyRepository),
            getNoteByIdUseCase = GetNoteByIdUseCase(emptyRepository)
        )

        composeTestRule.setContent {
            MaterialTheme {
                HomeScreen(
                    onAddNoteClick = {},
                    onNoteClick = {},
                    onChatClick = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithText("No notes available").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Create your first note to get started.")
            .assertIsDisplayed()
    }

    private class FakeNoteRepository(
        initialNotes: List<Note>
    ) : NoteRepository {

        private val notes = MutableStateFlow(initialNotes)

        override fun getAllNotes(): Flow<List<Note>> = notes

        override suspend fun getNoteById(id: String): Note? {
            return notes.value.find { it.id == id }
        }

        override suspend fun saveNote(note: Note) {
            notes.value = notes.value.filterNot { it.id == note.id } + note
        }

        override suspend fun deleteNote(id: String) {
            notes.value = notes.value.filterNot { it.id == id }
        }
    }
}
