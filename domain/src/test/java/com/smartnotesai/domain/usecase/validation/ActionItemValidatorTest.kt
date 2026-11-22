package com.smartnotesai.domain.usecase.validation

import com.smartnotesai.domain.model.ActionItem
import com.smartnotesai.domain.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class ActionItemValidatorTest {

    @Test
    fun `valid action item is accepted`() {
        val item = ActionItem(
            task = "Complete testing",
            assignedTo = "Rahul",
            deadline = "2026-11-20",
            priority = TaskPriority.HIGH
        )

        val result = ActionItemValidator.validate(item)

        assertEquals("Complete testing", result.task)
        assertEquals(TaskPriority.HIGH, result.priority)
    }

    @Test
    fun `empty task is rejected`() {
        val item = ActionItem(
            task = "",
            assignedTo = null,
            deadline = null,
            priority = null
        )

        assertThrows(IllegalArgumentException::class.java) {
            ActionItemValidator.validate(item)
        }
    }

    @Test
    fun `whitespace only task is rejected`() {
        val item = ActionItem(
            task = "   ",
            assignedTo = null,
            deadline = null,
            priority = null
        )

        assertThrows(IllegalArgumentException::class.java) {
            ActionItemValidator.validate(item)
        }
    }

    @Test
    fun `task description is trimmed`() {
        val item = ActionItem(
            task = "  Complete testing  ",
            assignedTo = null,
            deadline = null,
            priority = null
        )

        val result = ActionItemValidator.validate(item)

        assertEquals("Complete testing", result.task)
    }

    @Test
    fun `blank optional fields become null`() {
        val item = ActionItem(
            task = "Complete testing",
            assignedTo = "   ",
            deadline = "",
            priority = null
        )

        val result = ActionItemValidator.validate(item)

        assertNull(result.assignedTo)
        assertNull(result.deadline)
    }
}
