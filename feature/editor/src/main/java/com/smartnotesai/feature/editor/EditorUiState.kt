package com.smartnotesai.feature.editor

import com.smartnotesai.domain.model.MeetingSummary

data class EditorUiState(
    val noteId: String? = null,
    val title: String = "",
    val content: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val isSummarizing: Boolean = false,
    val summary: String? = null,
    val selectedRewriteStyle: String = "Professional",
    val isRewriting: Boolean = false,
    val rewrittenText: String? = null,
    val isProofreading: Boolean = false,
    val correctedText: String? = null,
    val isGeneratingMeetingSummary: Boolean = false,
    val meetingSummary: MeetingSummary? = null,
)