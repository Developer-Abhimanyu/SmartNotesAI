package com.smartnotesai.domain.usecase.validation

object AiInputValidator {

    const val MAX_NOTE_LENGTH = 20_000
    const val MAX_PROMPT_LENGTH = 25_000
    const val MAX_MEETING_NOTES_LENGTH = 20_000

    fun validateNoteContent(content: String) {
        require(content.isNotBlank()) {
            "Note content cannot be empty"
        }

        require(content.length <= MAX_NOTE_LENGTH) {
            "Note content exceeds the maximum allowed length"
        }
    }

    fun validatePrompt(prompt: String) {
        require(prompt.isNotBlank()) {
            "AI prompt cannot be empty"
        }

        require(prompt.length <= MAX_PROMPT_LENGTH) {
            "AI prompt exceeds the maximum allowed length"
        }
    }

    fun validateMeetingNotes(notes: String) {
        require(notes.isNotBlank()) {
            "Meeting notes cannot be empty"
        }

        require(notes.length <= MAX_MEETING_NOTES_LENGTH) {
            "Meeting notes exceed the maximum allowed length"
        }
    }
}
