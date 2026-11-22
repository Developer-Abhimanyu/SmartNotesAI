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
    data object SummarizeNote : EditorUiAction
    data class RewriteStyleChanged(val style: String) : EditorUiAction
    data object RewriteText : EditorUiAction
    data object ClearRewriteResult : EditorUiAction
    data object ProofreadText : EditorUiAction
    data object ClearCorrectionResult : EditorUiAction
    data object GenerateMeetingSummary : EditorUiAction
}