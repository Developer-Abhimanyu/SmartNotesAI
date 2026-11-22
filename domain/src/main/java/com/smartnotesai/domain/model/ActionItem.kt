package com.smartnotesai.domain.model

data class ActionItem(
    val task: String,
    val assignedTo: String?,
    val deadline: String?,
    val priority: TaskPriority?
)