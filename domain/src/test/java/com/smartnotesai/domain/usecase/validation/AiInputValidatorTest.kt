package com.smartnotesai.domain.usecase.validation

import org.junit.Assert.assertThrows
import org.junit.Test

class AiInputValidatorTest {

    @Test
    fun `validateNoteContent accepts valid content`() {
        AiInputValidator.validateNoteContent("My meeting notes")
    }

    @Test
    fun `validateNoteContent rejects blank content`() {
        assertThrows(IllegalArgumentException::class.java) {
            AiInputValidator.validateNoteContent("   ")
        }
    }

    @Test
    fun `validateNoteContent accepts maximum length`() {
        val content = "a".repeat(AiInputValidator.MAX_NOTE_LENGTH)

        AiInputValidator.validateNoteContent(content)
    }

    @Test
    fun `validateNoteContent rejects oversized content`() {
        val content = "a".repeat(AiInputValidator.MAX_NOTE_LENGTH + 1)

        assertThrows(IllegalArgumentException::class.java) {
            AiInputValidator.validateNoteContent(content)
        }
    }

    @Test
    fun `validatePrompt rejects blank prompt`() {
        assertThrows(IllegalArgumentException::class.java) {
            AiInputValidator.validatePrompt("")
        }
    }

    @Test
    fun `validatePrompt rejects oversized prompt`() {
        val prompt = "a".repeat(AiInputValidator.MAX_PROMPT_LENGTH + 1)

        assertThrows(IllegalArgumentException::class.java) {
            AiInputValidator.validatePrompt(prompt)
        }
    }

    @Test
    fun `validatePrompt accepts valid prompt`() {
        AiInputValidator.validatePrompt("Summarize this note")
    }

    @Test
    fun `validateMeetingNotes rejects blank notes`() {
        assertThrows(IllegalArgumentException::class.java) {
            AiInputValidator.validateMeetingNotes(" ")
        }
    }

    @Test
    fun `validateMeetingNotes accepts valid notes`() {
        AiInputValidator.validateMeetingNotes("Discussed project milestones")
    }

    @Test
    fun `validateMeetingNotes rejects oversized notes`() {
        val notes = "a".repeat(
            AiInputValidator.MAX_MEETING_NOTES_LENGTH + 1
        )

        assertThrows(IllegalArgumentException::class.java) {
            AiInputValidator.validateMeetingNotes(notes)
        }
    }
}
