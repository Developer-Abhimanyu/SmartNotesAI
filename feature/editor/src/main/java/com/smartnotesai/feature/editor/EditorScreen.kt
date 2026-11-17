package com.smartnotesai.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun EditorRoute(
    noteId: String?,
    onSaveSuccess: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(noteId) {
        viewModel.initialize(noteId)
    }
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaveSuccess()
        }
    }
    EditorScreen(
        title = uiState.title,
        content = uiState.content,
        onTitleChange = { viewModel.onAction(EditorUiAction.TitleChanged(it)) },
        onContentChange = { viewModel.onAction(EditorUiAction.ContentChanged(it)) },
        onSave = { viewModel.onAction(EditorUiAction.SaveNote) },
        isSaving = uiState.isSaving,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onClearError = {
            viewModel.onAction(EditorUiAction.ClearError)
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun EditorScreen(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean = false,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onClearError: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Edit Note", style = MaterialTheme.typography.headlineMedium)

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                placeholder = { Text("Enter note title") },
                singleLine = true,
                enabled = !isSaving
            )

            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text("Content") },
                placeholder = { Text("Write your note here...") },
                enabled = !isSaving
            )

            errorMessage?.let { message ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )

                    TextButton(onClick = onClearError) {
                        Text("Dismiss")
                    }
                }
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                Text(if (isSaving) "Saving..." else "Save Note")
            }
        }
    }
}