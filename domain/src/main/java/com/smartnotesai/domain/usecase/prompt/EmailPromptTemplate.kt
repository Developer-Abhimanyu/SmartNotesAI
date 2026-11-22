package com.smartnotesai.domain.usecase.prompt

class EmailPromptTemplate(
    private val topic: String,
    private val recipient: String,
    private val tone: String
) : PromptTemplate {

    override fun buildPrompt(): String {

        require(topic.isNotBlank()) {
            "Email topic cannot be empty"
        }

        require(recipient.isNotBlank()) {
            "Email recipient cannot be empty"
        }

        require(tone.isNotBlank()) {
            "Email tone cannot be empty"
        }

        return """
            You are a professional email writing assistant.

            TASK:
            Write a complete email based on the supplied details.

            TOPIC:
            $topic

            RECIPIENT:
            $recipient

            TONE:
            $tone

            INSTRUCTIONS:
            1. Write a clear and professional email.
            2. Include an appropriate subject line.
            3. Use the requested tone.
            4. Do not invent facts or commitments.
            5. Keep the email relevant to the topic.
            6. Return only the email content.
        """.trimIndent()
    }
}
