package com.smartnotesai.data.ai.retry

import com.smartnotesai.domain.model.AiError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AiRetryExecutorTest {

    @Test
    fun `retries network error and eventually succeeds`() = runBlocking {
        var requestCount = 0
        val delays = mutableListOf<Long>()

        val executor = AiRetryExecutor(
            wait = { delays.add(it) },
            jitterProvider = { 0L }
        )

        val result = executor.execute {
            requestCount++

            if (requestCount < 3) {
                throw AiError.Network()
            }

            "Success"
        }

        assertEquals("Success", result)
        assertEquals(3, requestCount)
        assertEquals(listOf(1_000L, 2_000L), delays)
    }

    @Test
    fun `does not retry safety blocked error`() = runBlocking {
        var requestCount = 0

        val executor = AiRetryExecutor(
            wait = {},
            jitterProvider = { 0L }
        )

        assertThrows(AiError.SafetyBlocked::class.java) {
            runBlocking {
                executor.execute {
                    requestCount++
                    throw AiError.SafetyBlocked()
                }
            }
        }

        assertEquals(1, requestCount)
    }

    @Test
    fun `stops after maximum retries`() = runBlocking {
        var requestCount = 0

        val executor = AiRetryExecutor(
            wait = {},
            jitterProvider = { 0L }
        )

        assertThrows(AiError.Network::class.java) {
            runBlocking {
                executor.execute {
                    requestCount++
                    throw AiError.Network()
                }
            }
        }

        // One initial request + three retries.
        assertEquals(4, requestCount)
    }

    @Test
    fun `does not retry cancelled request`() = runBlocking {
        var requestCount = 0

        val executor = AiRetryExecutor(
            wait = {},
            jitterProvider = { 0L }
        )

        assertThrows(CancellationException::class.java) {
            runBlocking {
                executor.execute {
                    requestCount++
                    throw CancellationException("Request cancelled")
                }
            }
        }

        assertEquals(1, requestCount)
    }
}