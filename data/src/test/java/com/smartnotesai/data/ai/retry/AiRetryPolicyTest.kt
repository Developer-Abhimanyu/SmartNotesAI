package com.smartnotesai.data.ai.retry

import com.smartnotesai.domain.model.AiError
import org.junit.Assert.assertEquals
import org.junit.Test

class AiRetryPolicyTest {

    @Test
    fun `network error is retryable`() {
        assertEquals(
            true,
            AiRetryPolicy.shouldRetry(AiError.Network())
        )
    }

    @Test
    fun `timeout error is retryable`() {
        assertEquals(
            true,
            AiRetryPolicy.shouldRetry(AiError.Timeout())
        )
    }

    @Test
    fun `server error is retryable`() {
        assertEquals(
            true,
            AiRetryPolicy.shouldRetry(AiError.Server())
        )
    }

    @Test
    fun `rate limit error is not retryable`() {
        assertEquals(
            false,
            AiRetryPolicy.shouldRetry(AiError.RateLimit())
        )
    }

    @Test
    fun `safety blocked error is not retryable`() {
        assertEquals(
            false,
            AiRetryPolicy.shouldRetry(AiError.SafetyBlocked())
        )
    }

    @Test
    fun `configuration error is not retryable`() {
        assertEquals(
            false,
            AiRetryPolicy.shouldRetry(AiError.Configuration())
        )
    }

    @Test
    fun `unknown error is not retryable`() {
        assertEquals(
            false,
            AiRetryPolicy.shouldRetry(AiError.Unknown())
        )
    }

    @Test
    fun `maximum retries is three`() {
        assertEquals(3, AiRetryPolicy.MAX_RETRIES)
    }
}
