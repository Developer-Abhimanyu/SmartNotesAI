package com.smartnotesai.feature.editor

import com.smartnotesai.domain.model.AiError

internal fun AiError.toUserMessage(): String {
    return when (this) {
        is AiError.Network ->
            "Check your internet connection and try again."

        is AiError.RateLimit ->
            "AI service limit reached. Please try later."

        is AiError.Timeout ->
            "The AI request timed out. Please try again."

        is AiError.SafetyBlocked ->
            "This request could not be processed."

        is AiError.Configuration ->
            "AI service is not configured correctly."

        is AiError.Server ->
            "AI service is temporarily unavailable."

        is AiError.Unknown ->
            "Something went wrong. Please try again."
    }
}