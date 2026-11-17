package com.smartnotesai.feature.home

import com.smartnotesai.domain.model.Note

data class HomeUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedNoteId: String? = null,
    val searchQuery: String = "",
    val sortOrder: NoteSortOrder = NoteSortOrder.RECENTLY_UPDATED
)