package com.smartnotesai.data.ai.retry

import com.smartnotesai.domain.model.AiError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.random.Random

class AiRetryExecutor(
    private val wait: suspend (Long) -> Unit = { delay(it) },
    private val jitterProvider: () -> Long = {
        Random.nextLong(0, AiRetryDelay.MAX_JITTER_MILLIS + 1)
    }
) {

    suspend fun <T> execute(
        request: suspend () -> T
    ): T {

        var retryAttempt = 0

        while (true) {
            try {
                return request()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: AiError) {

                if (
                    !AiRetryPolicy.shouldRetry(exception) ||
                    retryAttempt >= AiRetryPolicy.MAX_RETRIES
                ) {
                    throw exception
                }

                retryAttempt++

                val delayMillis = AiRetryDelay.calculateDelayMillis(
                    retryAttempt = retryAttempt,
                    jitterMillis = jitterProvider()
                )

                wait(delayMillis)
            }
        }
    }
}