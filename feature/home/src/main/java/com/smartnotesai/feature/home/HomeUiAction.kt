package com.smartnotesai.feature.home

sealed interface HomeUiAction {
    data object LoadNotes : HomeUiAction
    data object AddNote : HomeUiAction
    data class OpenNote(val noteId: String) : HomeUiAction
    data class DeleteNote(val noteId: String) : HomeUiAction
    data class SearchQueryChanged(val query: String) : HomeUiAction
    data class SortOrderChanged(val sortOrder: NoteSortOrder) : HomeUiAction
}