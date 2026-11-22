package com.smartnotesai.data.ai.parser

import com.smartnotesai.data.ai.model.MeetingSummaryDto
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object MeetingSummaryParser {

    const val MAX_RESPONSE_LENGTH = 50_000

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = false
    }

    fun parse(response: String): MeetingSummaryDto {

        require(response.isNotBlank()) {
            "AI response cannot be empty"
        }

        require(response.length <= MAX_RESPONSE_LENGTH) {
            "AI response exceeds the maximum allowed length"
        }

        return try {
            json.decodeFromString<MeetingSummaryDto>(response)
        } catch (exception: SerializationException) {
            throw AiResponseParsingException(
                message = "Gemini returned an invalid meeting summary response",
                cause = exception
            )
        }
    }
}
