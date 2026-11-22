package com.smartnotesai.domain.model

data class MeetingSummary(
    val summary: String,
    val decisions: List<String>,
    val actionItems: List<ActionItem>
)
