


package com.smartnotesai.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.smartnotesai.app.navigation.AppNavigation
import com.smartnotesai.app.ui.theme.SmartNotesAITheme
import com.smartnotesai.domain.usecase.CorrectGrammarUseCase
import com.smartnotesai.domain.usecase.GenerateTextUseCase
import com.smartnotesai.domain.usecase.RewriteTextUseCase
import com.smartnotesai.domain.usecase.SummarizeNoteUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var generateTextUseCase: GenerateTextUseCase

    @Inject
    lateinit var summarizeNoteUseCase: SummarizeNoteUseCase
    @Inject
    lateinit var rewriteTextUseCase: RewriteTextUseCase
    @Inject
    lateinit var correctGrammarUseCase: CorrectGrammarUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            var isLoading by remember { mutableStateOf(false) }
            var dialogMessage by remember { mutableStateOf<String?>(null) }

            SmartNotesAITheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),

                    /*floatingActionButton = {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    if (!isLoading) {
                                        lifecycleScope.launch {
                                            isLoading = true
                                            dialogMessage = null

                                            try {
                                                dialogMessage = summarizeNoteUseCase(
                                                    """
                                Today we discussed the new Android application architecture.
                                We decided to use MVVM, Jetpack Compose, Hilt for dependency
                                injection, and Room for local storage.
                                The team will complete the database module by Friday.
                                """.trimIndent()
                                                )
                                            } catch (exception: CancellationException) {
                                                throw exception
                                            } catch (exception: Exception) {
                                                dialogMessage =
                                                    "Summarization failed: ${exception.message}"
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text("Test Summary")
                            }

                            FloatingActionButton(
                                onClick = {
                                    // Keep your existing Test Gemini button implementation here.
                                }
                            ) {
                                Text("Test Gemini")
                            }
                            FloatingActionButton(
                                onClick = {
                                    if (!isLoading) {
                                        lifecycleScope.launch {
                                            isLoading = true
                                            dialogMessage = null

                                            try {
                                                val originalText = """
                        hey team, we need to finish the android database
                        implementation by friday. please check the room
                        entities and dao classes and complete your work.
                    """.trimIndent()

                                                val rewrittenText = withTimeout(60_000) {
                                                    rewriteTextUseCase(
                                                        originalText = originalText,
                                                        style = "Concise"
                                                    )
                                                }

                                                dialogMessage = """
                        Professional Rewrite:

                        $rewrittenText
                    """.trimIndent()

                                            } catch (exception: TimeoutCancellationException) {
                                                dialogMessage = "Rewrite request timed out. Please try again."
                                            } catch (exception: CancellationException) {
                                                throw exception
                                            } catch (exception: Exception) {
                                                dialogMessage = "Rewrite failed. Please check Logcat."
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text("Test Rewrite")
                            }

                            FloatingActionButton(
                                onClick = {
                                    if (!isLoading) {
                                        lifecycleScope.launch {
                                            isLoading = true
                                            dialogMessage = null

                                            try {
                                                val originalText = """
                        Yesterday i go to office and meet my manager.
                        he tell me the project work are not completed.
                    """.trimIndent()

                                                val correctedText = withTimeout(60_000) {
                                                    correctGrammarUseCase(originalText)
                                                }

                                                dialogMessage = """
                        Grammar Correction Result:

                        $correctedText
                    """.trimIndent()

                                            } catch (exception: TimeoutCancellationException) {
                                                dialogMessage = "Grammar correction timed out. Please try again."
                                            } catch (exception: CancellationException) {
                                                throw exception
                                            } catch (exception: Exception) {
                                                dialogMessage = "Grammar correction failed. Please check Logcat."
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text("Test Grammar")
                            }
                        }
                    }*/

                ) { innerPadding ->

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AppNavigation()
                    }
                }

                dialogMessage?.let { message ->
                    AlertDialog(
                        onDismissRequest = {
                            dialogMessage = null
                        },
                        title = {
                            Text("Gemini Connection Test")
                        },
                        text = {
                            Text(message)
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    dialogMessage = null
                                }
                            ) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }
}
