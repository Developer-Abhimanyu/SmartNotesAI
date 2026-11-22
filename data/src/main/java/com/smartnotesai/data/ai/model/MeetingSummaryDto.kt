package com.smartnotesai.data.ai.model

import kotlinx.serialization.Serializable

@Serializable
data class MeetingSummaryDto(
    val summary: String,
    val decisions: List<String>,
    val actionItems: List<ActionItemDto>
)

@Serializable
data class ActionItemDto(
    val task: String,
    val assignedTo: String? = null,
    val deadline: String? = null,
    val priority: String? = null
)