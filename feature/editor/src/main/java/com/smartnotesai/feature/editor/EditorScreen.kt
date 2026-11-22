package com.smartnotesai.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartnotesai.domain.model.MeetingSummary

private val rewriteStyles = listOf(
    "Professional",
    "Friendly",
    "Formal",
    "Concise"
)

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
        modifier = Modifier.fillMaxSize(),
        isSummarizing = uiState.isSummarizing,
        summary = uiState.summary,
        onSummarize = {
            viewModel.onAction(EditorUiAction.SummarizeNote)
        },
        selectedRewriteStyle = uiState.selectedRewriteStyle,
        onRewriteStyleChange = {
            viewModel.onAction(
                EditorUiAction.RewriteStyleChanged(it)
            )
        },
        isRewriting = uiState.isRewriting,
        rewrittenText = uiState.rewrittenText,
        onRewrite = {
            viewModel.onAction(EditorUiAction.RewriteText)
        },
        onApplyRewrite = {
            uiState.rewrittenText?.let { result ->
                viewModel.onAction(
                    EditorUiAction.ContentChanged(result)
                )
                viewModel.onAction(
                    EditorUiAction.ClearRewriteResult
                )
            }
        },
        onDiscardRewrite = {
            viewModel.onAction(
                EditorUiAction.ClearRewriteResult
            )
        },
        isProofreading = uiState.isProofreading,
        correctedText = uiState.correctedText,
        onProofread = {
            viewModel.onAction(EditorUiAction.ProofreadText)
        },
        onApplyCorrection = {
            uiState.correctedText?.let { result ->
                viewModel.onAction(EditorUiAction.ContentChanged(result))
                viewModel.onAction(EditorUiAction.ClearCorrectionResult)
            }
        },
        onDiscardCorrection = {
            viewModel.onAction(EditorUiAction.ClearCorrectionResult)
        },
        isGeneratingMeetingSummary = uiState.isGeneratingMeetingSummary,
        meetingSummary = uiState.meetingSummary,
        onGenerateMeetingSummary = {
            viewModel.onAction(EditorUiAction.GenerateMeetingSummary)
        }
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
    isSummarizing: Boolean = false,
    summary: String? = null,
    onSummarize: () -> Unit,
    selectedRewriteStyle: String = "Professional",
    onRewriteStyleChange: (String) -> Unit,
    isRewriting: Boolean = false,
    rewrittenText: String? = null,
    onRewrite: () -> Unit,
    onApplyRewrite: () -> Unit,
    onDiscardRewrite: () -> Unit,
    isProofreading: Boolean = false,
    correctedText: String? = null,
    onProofread: () -> Unit,
    onApplyCorrection: () -> Unit,
    onDiscardCorrection: () -> Unit,
    isGeneratingMeetingSummary: Boolean = false,
    meetingSummary: MeetingSummary? = null,
    onGenerateMeetingSummary: () -> Unit
) {

    var isStyleMenuExpanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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

            Button(
                onClick = onSummarize,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving && !isSummarizing
            ) {
                Text(
                    if (isSummarizing) "Summarizing..." else "Summarize Note"
                )
            }

            if (isSummarizing) {
                CircularProgressIndicator()
            }

            summary?.let { result ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "AI Summary",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = result,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Text(
                text = "AI Meeting Summary (Structured JSON)",
                style = MaterialTheme.typography.titleLarge
            )

            Button(
                onClick = onGenerateMeetingSummary,
                modifier = Modifier.fillMaxWidth(),
                enabled = content.isNotBlank() &&
                        !isSaving &&
                        !isGeneratingMeetingSummary &&
                        !isRewriting &&
                        !isProofreading &&
                        !isSummarizing
            ) {
                Text(
                    if (isGeneratingMeetingSummary) {
                        "Generating Meeting Summary..."
                    } else {
                        "Generate Meeting Summary"
                    }
                )
            }

            if (isGeneratingMeetingSummary) {
                CircularProgressIndicator()
                Text("Gemini is analyzing your meeting notes...")
            }

            meetingSummary?.let { result ->

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = "Meeting Summary",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = result.summary,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Key Decisions",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (result.decisions.isEmpty()) {
                        Text("No decisions identified.")
                    } else {
                        result.decisions.forEachIndexed { index, decision ->
                            Text("${index + 1}. $decision")
                        }
                    }

                    Text(
                        text = "Action Items",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (result.actionItems.isEmpty()) {
                        Text("No action items identified.")
                    } else {
                        result.actionItems.forEachIndexed { index, item ->

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${index + 1}. ${item.task}",
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text("Assigned To: ${item.assignedTo ?: "Not specified"}")

                                Text("Deadline: ${item.deadline ?: "Not specified"}")

                                Text("Priority: ${item.priority ?: "Not specified"}")
                            }
                        }
                    }
                }
            }

            Text(
                text = "AI Text Rewriting",
                style = MaterialTheme.typography.titleLarge
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = "Writing Style",
                    style = MaterialTheme.typography.titleMedium
                )

                Box {
                    Button(
                        onClick = {
                            isStyleMenuExpanded = true
                        },
                        enabled = !isSaving && !isRewriting
                    ) {
                        Text("Choose Style: $selectedRewriteStyle")
                    }

                    DropdownMenu(
                        expanded = isStyleMenuExpanded,
                        onDismissRequest = {
                            isStyleMenuExpanded = false
                        }
                    ) {
                        rewriteStyles.forEach { style ->
                            DropdownMenuItem(
                                text = {
                                    Text(style)
                                },
                                onClick = {
                                    onRewriteStyleChange(style)
                                    isStyleMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = onRewrite,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = content.isNotBlank() &&
                            !isSaving &&
                            !isRewriting &&
                            !isSummarizing
                ) {
                    Text(
                        if (isRewriting) {
                            "Rewriting..."
                        } else {
                            "Rewrite Text"
                        }
                    )
                }

                if (isRewriting) {
                    CircularProgressIndicator()
                    Text("Gemini is rewriting your text...")
                }

                rewrittenText?.let { result ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Rewritten Text",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = onApplyRewrite,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving
                        ) {
                            Text("Apply Rewritten Text")
                        }

                        TextButton(
                            onClick = onDiscardRewrite,
                            enabled = !isSaving
                        ) {
                            Text("Discard Rewrite")
                        }
                    }
                }

                Text(
                    text = "AI Grammar Correction",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Correct grammar, spelling, punctuation and sentence structure.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = onProofread,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = content.isNotBlank() && !isSaving &&
                            !isProofreading && !isRewriting && !isSummarizing
                ) {
                    Text(
                        if (isProofreading) {
                            "Proofreading..."
                        } else {
                            "Proofread Text"
                        }
                    )
                }

                if (isProofreading) {
                    CircularProgressIndicator()
                    Text("Gemini is checking your text...")
                }

                correctedText?.let { result ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Corrected Text",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = onApplyCorrection,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving
                        ) {
                            Text("Apply Correction")
                        }

                        TextButton(
                            onClick = onDiscardCorrection,
                            enabled = !isSaving
                        ) {
                            Text("Discard Correction")
                        }
                    }
                }
            }

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