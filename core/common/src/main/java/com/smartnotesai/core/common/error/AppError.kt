package com.smartnotesai.core.common.error

sealed interface AppError {

    data object Network : AppError

    data object Unauthorized : AppError

    data object RateLimitExceeded : AppError

    data object ServiceUnavailable : AppError

    data object InvalidInput : AppError

    data object Storage : AppError

    data object Unknown : AppError
}
