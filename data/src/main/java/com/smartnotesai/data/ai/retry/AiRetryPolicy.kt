package com.smartnotesai.data.ai.retry

import com.smartnotesai.domain.model.AiError

object AiRetryPolicy {

    const val MAX_RETRIES = 3

    fun shouldRetry(error: AiError): Boolean {
        return when (error) {
            is AiError.Network,
            is AiError.Timeout,
            is AiError.Server -> true

            is AiError.RateLimit,
            is AiError.SafetyBlocked,
            is AiError.Configuration,
            is AiError.Unknown -> false
        }
    }
}
