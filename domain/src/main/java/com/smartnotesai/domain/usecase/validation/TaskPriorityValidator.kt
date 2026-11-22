package com.smartnotesai.domain.usecase.validation

import com.smartnotesai.domain.model.TaskPriority

object TaskPriorityValidator {

    fun validate(value: String?): TaskPriority? {
        if (value.isNullOrBlank()) {
            return null
        }

        return try {
            TaskPriority.valueOf(value.trim().uppercase())
        } catch (exception: IllegalArgumentException) {
            throw IllegalArgumentException(
                "Invalid task priority: $value"
            )
        }
    }
}
