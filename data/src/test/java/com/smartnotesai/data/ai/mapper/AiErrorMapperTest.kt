package com.smartnotesai.data.ai.mapper

import com.smartnotesai.data.ai.parser.AiResponseParsingException
import com.smartnotesai.domain.model.AiError
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class AiErrorMapperTest {

    @Test
    fun `IOException maps to network error`() {
        val cause = IOException("Connection failed")

        val result = cause.toAiError()

        assertEquals(true, result is AiError.Network)
        assertSame(cause, result.cause)
    }

    @Test
    fun `unexpected exception maps to unknown error`() {
        val cause = IllegalStateException("Unexpected failure")

        val result = cause.toAiError()

        assertEquals(true, result is AiError.Unknown)
        assertSame(cause, result.cause)
    }

    @Test
    fun `cancellation exception is rethrown`() {
        val cause = CancellationException("Request cancelled")

        val result = assertThrows(CancellationException::class.java) {
            cause.toAiError()
        }

        assertSame(cause, result)
    }


    @Test
    fun `AI response parsing exception maps to unknown error`() {
        val cause = AiResponseParsingException(
            "Invalid meeting summary response"
        )

        val result = cause.toAiError()

        assertEquals(true, result is AiError.Unknown)
        assertSame(cause, result.cause)
    }

}
