package com.smartnotesai.domain.usecase.validation

import com.smartnotesai.domain.model.ActionItem

object ActionItemValidator {

    fun validate(item: ActionItem): ActionItem {
        require(item.task.isNotBlank()) {
            "Action item task cannot be empty"
        }

        return item.copy(
            task = item.task.trim(),
            assignedTo = item.assignedTo?.trim()?.ifBlank { null },
            deadline = item.deadline?.trim()?.ifBlank { null }
        )
    }
}
