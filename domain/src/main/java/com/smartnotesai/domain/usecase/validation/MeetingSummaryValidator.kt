package com.smartnotesai.domain.usecase.validation

import com.smartnotesai.domain.model.MeetingSummary

object MeetingSummaryValidator {

    fun validate(summary: MeetingSummary): MeetingSummary {

        require(summary.summary.isNotBlank()) {
            "Meeting summary cannot be empty"
        }

        require(summary.decisions.all { it.isNotBlank() }) {
            "Meeting decisions cannot contain empty values"
        }

        return summary.copy(
            summary = summary.summary.trim(),
            decisions = summary.decisions.map { it.trim() },
            actionItems = summary.actionItems.map {
                ActionItemValidator.validate(it)
            }
        )
    }
}
