package com.smartnotesai.feature.chat

import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.smartnotesai.domain.model.ChatMessage
import com.smartnotesai.domain.model.ChatRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.ImageBitmap

@Composable
fun ChatRoute() {
    val viewModel: ChatViewModel = hiltViewModel()
    ChatScreen(viewModel = viewModel)
}

@Composable
fun ChatScreen(
    viewModel: ChatViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Android Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->

        uri?.let {
            viewModel.onAction(
                ChatUiAction.ImageSelected(it)
            )
        }
    }

    LaunchedEffect(
        uiState.messages.lastOrNull()?.content,
        uiState.messages.size
    ) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(
                uiState.messages.lastIndex
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SmartNotes AI Chat",
                style = MaterialTheme.typography.headlineSmall
            )

            TextButton(
                onClick = {
                    viewModel.onAction(
                        ChatUiAction.ClearConversation
                    )
                },
                enabled = !uiState.isStreaming &&
                        !uiState.isLoadingHistory &&
                        (
                                uiState.conversation != null ||
                                        uiState.messages.isNotEmpty() ||
                                        uiState.prompt.isNotEmpty()
                                )
            ) {
                Text("New Chat")
            }
        }

        // Previous conversations
        Text(
            text = "Previous Conversations",
            style = MaterialTheme.typography.titleMedium
        )

        if (uiState.conversations.isEmpty()) {
            Text(
                text = "No previous conversations yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.conversations,
                    key = { conversation -> conversation.id }
                ) { conversation ->

                    val isSelected =
                        uiState.conversation?.id == conversation.id

                    Surface(
                        modifier = Modifier.clickable(
                            enabled = !uiState.isStreaming &&
                                    !uiState.isLoadingHistory
                        ) {
                            viewModel.onAction(
                                ChatUiAction.ConversationSelected(
                                    conversation.id
                                )
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = conversation.title,
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }

        // Loading previous messages
        if (uiState.isLoadingHistory) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp)
                )

                Text("Loading conversation...")
            }
        }

        // Chat messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = uiState.messages,
                key = { message -> message.id }
            ) { message ->
                ChatMessageItem(message = message)
            }

            if (uiState.isStreaming) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(4.dp)
                        )

                        Text("Gemini is generating a response...")
                    }
                }
            }

            uiState.errorMessage?.let { error ->
                item {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    TextButton(
                        onClick = {
                            viewModel.onAction(
                                ChatUiAction.ClearError
                            )
                        }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }

        // Selected image preview
        uiState.selectedImageUri?.let { uri ->
            SelectedImagePreview(
                uri = uri,
                onRemove = {
                    viewModel.onAction(
                        ChatUiAction.RemoveSelectedImage
                    )
                }
            )
        }

        // Image picker button
        OutlinedButton(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            enabled = !uiState.isStreaming &&
                    !uiState.isLoadingHistory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (uiState.selectedImageUri == null) {
                    "Attach Image"
                } else {
                    "Choose Another Image"
                }
            )
        }

        // Message input
        OutlinedTextField(
            value = uiState.prompt,
            onValueChange = { prompt ->
                viewModel.onAction(
                    ChatUiAction.PromptChanged(prompt)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Ask SmartNotes AI")
            },
            placeholder = {
                Text("Enter your question...")
            },
            enabled = !uiState.isStreaming &&
                    !uiState.isLoadingHistory,
            maxLines = 4
        )

        // Send message or image
        Button(
            onClick = {
                viewModel.onAction(ChatUiAction.SendMessage)
            },
            enabled = !uiState.isStreaming &&
                    !uiState.isLoadingHistory &&
                    (
                            uiState.prompt.isNotBlank() ||
                                    uiState.selectedImageUri != null
                            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send")
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage
) {
    val isUser = message.role == ChatRole.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            horizontalAlignment = if (isUser) {
                Alignment.End
            } else {
                Alignment.Start
            },
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isUser) "YOU" else "SMARTNOTES AI",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isUser) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = message.content.ifBlank {
                        if (!isUser) {
                            "Generating response..."
                        } else {
                            ""
                        }
                    },
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isUser) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectedImagePreview(
    uri: Uri,
    onRemove: () -> Unit
) {
    val context = LocalContext.current

    val imageBitmap by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = uri
    ) {
        value = withContext(Dispatchers.IO) {
            try {
                val source = ImageDecoder.createSource(
                    context.contentResolver,
                    uri
                )

                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.setTargetSize(512, 512)
                }.asImageBitmap()

            } catch (exception: Exception) {
                null
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap!!,
                contentDescription = "Selected image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = "Selected image preview unavailable",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Image selected",
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                text = "Ready for AI analysis",
                style = MaterialTheme.typography.bodySmall
            )
        }

        TextButton(onClick = onRemove) {
            Text("Remove")
        }
    }
}
