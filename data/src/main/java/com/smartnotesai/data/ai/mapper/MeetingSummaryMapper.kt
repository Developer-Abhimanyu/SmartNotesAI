package com.smartnotesai.data.ai.mapper

import com.smartnotesai.data.ai.model.MeetingSummaryDto
import com.smartnotesai.domain.model.ActionItem
import com.smartnotesai.domain.model.MeetingSummary
import com.smartnotesai.domain.usecase.validation.ActionItemValidator
import com.smartnotesai.domain.usecase.validation.TaskPriorityValidator
import com.smartnotesai.domain.usecase.validation.MeetingSummaryValidator

fun MeetingSummaryDto.toDomain(): MeetingSummary {

    val meetingSummary = MeetingSummary(
        summary = summary,
        decisions = decisions,
        actionItems = actionItems.map { item ->
            ActionItemValidator.validate(
                ActionItem(
                    task = item.task,
                    assignedTo = item.assignedTo,
                    deadline = item.deadline,
                    priority = TaskPriorityValidator.validate(
                        item.priority
                    )
                )
            )
        }
    )

    return MeetingSummaryValidator.validate(meetingSummary)
}
