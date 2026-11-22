package com.smartnotesai.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartnotesai.feature.home.components.EmptyNotesView
import com.smartnotesai.feature.home.components.HomeTopBar
import com.smartnotesai.feature.home.components.NoteCard
import com.smartnotesai.feature.home.components.NoteSearchBar
import com.smartnotesai.feature.home.components.NoteSortSelector

@Composable
fun HomeScreen(
    onAddNoteClick: () -> Unit,
    onNoteClick: (String) -> Unit,
    onChatClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var noteToDelete by remember {
        mutableStateOf<String?>(null)
    }

    val filteredNotes = remember(
        uiState.notes,
        uiState.searchQuery,
        uiState.sortOrder
    ) {
        val query = uiState.searchQuery.trim()

        val matchingNotes = if (query.isBlank()) {
            uiState.notes
        } else {
            uiState.notes.filter { note ->
                note.title.contains(query, ignoreCase = true) ||
                        note.content.contains(query, ignoreCase = true)
            }
        }

        when (uiState.sortOrder) {
            NoteSortOrder.RECENTLY_UPDATED ->
                matchingNotes.sortedByDescending { it.updatedAt }

            NoteSortOrder.OLDEST_UPDATED ->
                matchingNotes.sortedBy { it.updatedAt }

            NoteSortOrder.ALPHABETICAL ->
                matchingNotes.sortedBy { it.title.lowercase() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HomeTopBar(noteCount = uiState.notes.size)
        NoteSearchBar(query = uiState.searchQuery, onQueryChange = { query ->
            viewModel.onAction(
                HomeUiAction.SearchQueryChanged(query)
            )
        })
        NoteSortSelector(selectedOrder = uiState.sortOrder, onOrderSelected = { order ->
            viewModel.onAction(HomeUiAction.SortOrderChanged(order))
        })
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAddNoteClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Add Note")
            }

            Button(
                onClick = onChatClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Chat with AI")
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else if (uiState.errorMessage != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = uiState.errorMessage ?: "Something went wrong",
                    color = MaterialTheme.colorScheme.error
                )

                Button(
                    onClick = {
                        viewModel.onAction(HomeUiAction.LoadNotes)
                    }
                ) {
                    Text("Retry")
                }
            }
        } else if (filteredNotes.isEmpty()) {
            if (uiState.searchQuery.isBlank()) {
                EmptyNotesView()
            } else {
                Text(
                    text = "No notes found",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = filteredNotes,
                    key = { note -> note.id }
                ) { note ->
                    NoteCard(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                        onDelete = { noteToDelete = note.id }
                    )
                }
            }
        }

        noteToDelete?.let { noteId ->

            AlertDialog(
                onDismissRequest = {
                    noteToDelete = null
                },
                title = {
                    Text("Delete Note?")
                },
                text = {
                    Text("Are you sure you want to delete this note? This action cannot be undone.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.onAction(
                                HomeUiAction.DeleteNote(noteId)
                            )
                            noteToDelete = null
                        }
                    ) {
                        Text(
                            "Delete",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            noteToDelete = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}