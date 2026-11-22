package com.smartnotesai.data.ai.mapper

import com.smartnotesai.data.ai.model.ActionItemDto
import com.smartnotesai.data.ai.model.MeetingSummaryDto
import com.smartnotesai.domain.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class MeetingSummaryMapperTest {

    @Test
    fun `valid DTO converts to Domain model`() {
        val dto = MeetingSummaryDto(
            summary = "Project discussion",
            decisions = listOf("Release on Friday"),
            actionItems = listOf(
                ActionItemDto(
                    task = "Complete testing",
                    assignedTo = "Rahul",
                    deadline = "2026-11-20",
                    priority = "HIGH"
                )
            )
        )

        val result = dto.toDomain()

        assertEquals("Project discussion", result.summary)
        assertEquals(TaskPriority.HIGH, result.actionItems[0].priority)
        assertEquals("Rahul", result.actionItems[0].assignedTo)
    }

    @Test
    fun `lowercase priority is converted correctly`() {
        val dto = MeetingSummaryDto(
            summary = "Project discussion",
            decisions = emptyList(),
            actionItems = listOf(
                ActionItemDto(
                    task = "Complete testing",
                    priority = "medium"
                )
            )
        )

        val result = dto.toDomain()

        assertEquals(TaskPriority.MEDIUM, result.actionItems[0].priority)
    }

    @Test
    fun `invalid priority is rejected`() {
        val dto = MeetingSummaryDto(
            summary = "Project discussion",
            decisions = emptyList(),
            actionItems = listOf(
                ActionItemDto(
                    task = "Complete testing",
                    priority = "URGENT"
                )
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            dto.toDomain()
        }
    }

    @Test
    fun `empty task description is rejected`() {
        val dto = MeetingSummaryDto(
            summary = "Project discussion",
            decisions = emptyList(),
            actionItems = listOf(
                ActionItemDto(task = "   ")
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            dto.toDomain()
        }
    }

    @Test
    fun `blank optional fields become null`() {
        val dto = MeetingSummaryDto(
            summary = "Project discussion",
            decisions = emptyList(),
            actionItems = listOf(
                ActionItemDto(
                    task = " Complete testing ",
                    assignedTo = "   ",
                    deadline = "",
                    priority = null
                )
            )
        )

        val result = dto.toDomain()
        val item = result.actionItems.first()

        assertEquals("Complete testing", item.task)
        assertNull(item.assignedTo)
        assertNull(item.deadline)
        assertNull(item.priority)
    }

    @Test
    fun `blank meeting summary is rejected`() {
        val dto = MeetingSummaryDto(
            summary = " ",
            decisions = emptyList(),
            actionItems = emptyList()
        )

        assertThrows(IllegalArgumentException::class.java) {
            dto.toDomain()
        }
    }

    @Test
    fun `blank decision is rejected`() {
        val dto = MeetingSummaryDto(
            summary = "Project discussion",
            decisions = listOf(""),
            actionItems = emptyList()
        )

        assertThrows(IllegalArgumentException::class.java) {
            dto.toDomain()
        }
    }

    @Test
    fun `summary and decisions are trimmed`() {
        val dto = MeetingSummaryDto(
            summary = " Project discussion ",
            decisions = listOf(" Release next month "),
            actionItems = emptyList()
        )

        val result = dto.toDomain()

        assertEquals("Project discussion", result.summary)
        assertEquals(
            listOf("Release next month"),
            result.decisions
        )
    }
}
