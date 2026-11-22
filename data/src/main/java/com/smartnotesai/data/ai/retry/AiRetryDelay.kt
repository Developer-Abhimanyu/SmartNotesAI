package com.smartnotesai.data.ai.retry

import kotlin.math.min

object AiRetryDelay {

    const val BASE_DELAY_MILLIS = 1_000L
    const val MAX_DELAY_MILLIS = 8_000L
    const val MAX_JITTER_MILLIS = 500L

    fun calculateDelayMillis(
        retryAttempt: Int,
        jitterMillis: Long
    ): Long {

        require(retryAttempt in 1..AiRetryPolicy.MAX_RETRIES) {
            "Invalid retry attempt"
        }

        require(jitterMillis in 0..MAX_JITTER_MILLIS) {
            "Invalid jitter value"
        }

        val exponentialDelay = BASE_DELAY_MILLIS *
                (1L shl (retryAttempt - 1))

        return min(
            exponentialDelay,
            MAX_DELAY_MILLIS
        ) + jitterMillis
    }
}
