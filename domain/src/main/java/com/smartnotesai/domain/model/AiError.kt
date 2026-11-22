package com.smartnotesai.domain.model

sealed class AiError(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class Network(
        cause: Throwable? = null
    ) : AiError("Unable to connect to the AI service.", cause)

    class RateLimit(
        cause: Throwable? = null
    ) : AiError("The AI service limit has been reached.", cause)

    class Timeout(
        cause: Throwable? = null
    ) : AiError("The AI request timed out.", cause)

    class SafetyBlocked(
        cause: Throwable? = null
    ) : AiError("This request could not be processed.", cause)

    class Configuration(
        cause: Throwable? = null
    ) : AiError("The AI service is not configured correctly.", cause)

    class Server(
        cause: Throwable? = null
    ) : AiError("The AI service is temporarily unavailable.", cause)

    class Unknown(
        cause: Throwable? = null
    ) : AiError("Something went wrong while processing your request.", cause)
}
