package com.smartnotesai.domain.usecase.validation

import com.smartnotesai.domain.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class TaskPriorityValidatorTest {

    @Test
    fun `valid HIGH priority returns HIGH`() {
        val result = TaskPriorityValidator.validate("HIGH")

        assertEquals(TaskPriority.HIGH, result)
    }

    @Test
    fun `lowercase priority is converted to uppercase`() {
        val result = TaskPriorityValidator.validate("medium")

        assertEquals(TaskPriority.MEDIUM, result)
    }

    @Test
    fun `priority with whitespace is trimmed`() {
        val result = TaskPriorityValidator.validate(" LOW ")

        assertEquals(TaskPriority.LOW, result)
    }

    @Test
    fun `null priority returns null`() {
        val result = TaskPriorityValidator.validate(null)

        assertNull(result)
    }

    @Test
    fun `blank priority returns null`() {
        val result = TaskPriorityValidator.validate("   ")

        assertNull(result)
    }

    @Test
    fun `unsupported priority throws exception`() {
        assertThrows(IllegalArgumentException::class.java) {
            TaskPriorityValidator.validate("URGENT")
        }
    }
}
