package com.smartnotesai.domain.usecase.validation

import com.smartnotesai.domain.model.ActionItem
import com.smartnotesai.domain.model.MeetingSummary
import com.smartnotesai.domain.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MeetingSummaryValidatorTest {

    @Test
    fun `valid meeting summary is accepted`() {
        val summary = MeetingSummary(
            summary = " Project discussion ",
            decisions = listOf(" Launch next month "),
            actionItems = emptyList()
        )

        val result = MeetingSummaryValidator.validate(summary)

        assertEquals("Project discussion", result.summary)
        assertEquals(listOf("Launch next month"), result.decisions)
    }

    @Test
    fun `blank summary is rejected`() {
        val summary = MeetingSummary(
            summary = " ",
            decisions = emptyList(),
            actionItems = emptyList()
        )

        assertThrows(IllegalArgumentException::class.java) {
            MeetingSummaryValidator.validate(summary)
        }
    }

    @Test
    fun `blank decision is rejected`() {
        val summary = MeetingSummary(
            summary = "Project discussion",
            decisions = listOf(""),
            actionItems = emptyList()
        )

        assertThrows(IllegalArgumentException::class.java) {
            MeetingSummaryValidator.validate(summary)
        }
    }

    @Test
    fun `action items are validated and normalized`() {
        val summary = MeetingSummary(
            summary = "Project discussion",
            decisions = emptyList(),
            actionItems = listOf(
                ActionItem(
                    task = " Complete testing ",
                    assignedTo = " Rahul ",
                    deadline = " ",
                    priority = TaskPriority.HIGH
                )
            )
        )

        val result = MeetingSummaryValidator.validate(summary)
        val actionItem = result.actionItems.first()

        assertEquals("Complete testing", actionItem.task)
        assertEquals("Rahul", actionItem.assignedTo)
        assertEquals(null, actionItem.deadline)
        assertEquals(TaskPriority.HIGH, actionItem.priority)
    }
}
