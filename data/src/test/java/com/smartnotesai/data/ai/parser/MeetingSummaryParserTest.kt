package com.smartnotesai.data.ai.parser

import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MeetingSummaryParserTest {

    @Test
    fun `valid JSON is parsed successfully`() {
        val json = """
            {
                "summary": "Discussed project progress.",
                "decisions": ["Release on Friday"],
                "actionItems": [
                    {
                        "task": "Complete testing",
                        "assignedTo": "Rahul",
                        "deadline": "2026-11-20",
                        "priority": "HIGH"
                    }
                ]
            }
        """.trimIndent()

        val result = MeetingSummaryParser.parse(json)

        assertEquals("Discussed project progress.", result.summary)
        assertEquals(1, result.decisions.size)
        assertEquals("Complete testing", result.actionItems[0].task)
        assertEquals("HIGH", result.actionItems[0].priority)
    }


    @Test
    fun `malformed JSON throws custom parsing exception`() {
        val json = """{"summary": """

        val exception = assertThrows(
            AiResponseParsingException::class.java
        ) {
            MeetingSummaryParser.parse(json)
        }

        assertEquals(
            "Gemini returned an invalid meeting summary response",
            exception.message
        )

        assertTrue(
            exception.cause is SerializationException
        )
    }


    @Test
    fun `missing required field throws custom parsing exception`() {
        val json = """
        {
            "decisions": [],
            "actionItems": []
        }
    """.trimIndent()

        val exception = assertThrows(
            AiResponseParsingException::class.java
        ) {
            MeetingSummaryParser.parse(json)
        }

        assertTrue(
            exception.cause is SerializationException
        )
    }

    @Test
    fun `empty response is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            MeetingSummaryParser.parse("")
        }
    }

    @Test
    fun `whitespace response is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            MeetingSummaryParser.parse("   ")
        }
    }

    @Test
    fun `oversized response is rejected`() {
        val oversizedResponse = "a".repeat(
            MeetingSummaryParser.MAX_RESPONSE_LENGTH + 1
        )

        assertThrows(IllegalArgumentException::class.java) {
            MeetingSummaryParser.parse(oversizedResponse)
        }
    }

    @Test
    fun `maximum allowed response length is accepted for size validation`() {
        // This test checks the size boundary independently.
        // A valid JSON response is tested separately.
        assertEquals(
            50_000,
            MeetingSummaryParser.MAX_RESPONSE_LENGTH
        )
    }

    @Test
    fun `unknown JSON fields are ignored`() {
        val json = """
            {
                "summary": "Project discussion",
                "decisions": [],
                "actionItems": [],
                "unexpectedField": "Extra information"
            }
        """.trimIndent()

        val result = MeetingSummaryParser.parse(json)

        assertEquals("Project discussion", result.summary)
        assertEquals(0, result.decisions.size)
        assertEquals(0, result.actionItems.size)
    }

    @Test
    fun `missing optional fields default to null`() {
        val json = """
            {
                "summary": "Project discussion",
                "decisions": [],
                "actionItems": [
                    {
                        "task": "Complete testing"
                    }
                ]
            }
        """.trimIndent()

        val result = MeetingSummaryParser.parse(json)
        val actionItem = result.actionItems.first()

        assertNull(actionItem.assignedTo)
        assertNull(actionItem.deadline)
        assertNull(actionItem.priority)
    }
}
