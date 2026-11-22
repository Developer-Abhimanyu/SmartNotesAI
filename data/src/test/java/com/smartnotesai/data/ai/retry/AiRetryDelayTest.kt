package com.smartnotesai.data.ai.retry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AiRetryDelayTest {

    @Test
    fun `first retry uses one second plus jitter`() {
        val result = AiRetryDelay.calculateDelayMillis(
            retryAttempt = 1,
            jitterMillis = 200
        )

        assertEquals(1_200L, result)
    }

    @Test
    fun `second retry uses two seconds plus jitter`() {
        val result = AiRetryDelay.calculateDelayMillis(
            retryAttempt = 2,
            jitterMillis = 300
        )

        assertEquals(2_300L, result)
    }

    @Test
    fun `third retry uses four seconds plus jitter`() {
        val result = AiRetryDelay.calculateDelayMillis(
            retryAttempt = 3,
            jitterMillis = 500
        )

        assertEquals(4_500L, result)
    }

    @Test
    fun `retry attempt zero is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            AiRetryDelay.calculateDelayMillis(0, 0)
        }
    }

    @Test
    fun `retry attempt above maximum is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            AiRetryDelay.calculateDelayMillis(4, 0)
        }
    }

    @Test
    fun `negative jitter is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            AiRetryDelay.calculateDelayMillis(1, -1)
        }
    }

    @Test
    fun `jitter above maximum is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            AiRetryDelay.calculateDelayMillis(1, 501)
        }
    }
}