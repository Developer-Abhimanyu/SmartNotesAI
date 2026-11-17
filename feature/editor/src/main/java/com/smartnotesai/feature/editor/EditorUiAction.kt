package com.smartnotesai.feature.editor

sealed interface EditorUiAction {

    data class TitleChanged(
        val title: String
    ) : EditorUiAction

    data class ContentChanged(
        val content: String
    ) : EditorUiAction

    data object LoadNote : EditorUiAction
    data object SaveNote : EditorUiAction
    data object ClearError : EditorUiAction
}