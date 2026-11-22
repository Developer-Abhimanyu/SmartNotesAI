package com.smartnotesai.data.ai.mapper

import com.google.firebase.ai.type.APINotConfiguredException
import com.google.firebase.ai.type.ContentBlockedException
import com.google.firebase.ai.type.InvalidAPIKeyException
import com.google.firebase.ai.type.InvalidLocationException
import com.google.firebase.ai.type.PermissionMissingException
import com.google.firebase.ai.type.PromptBlockedException
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.RequestTimeoutException
import com.google.firebase.ai.type.ResponseStoppedException
import com.google.firebase.ai.type.ServerException
import com.google.firebase.ai.type.ServiceConnectionHandshakeFailedException
import com.google.firebase.ai.type.ServiceDisabledException
import com.smartnotesai.domain.model.AiError
import kotlinx.coroutines.CancellationException
import java.io.IOException

fun Throwable.toAiError(): AiError {

    // Cancellation must never become an ordinary AI error.
    if (this is CancellationException) {
        throw this
    }

    return when (this) {

        is PromptBlockedException,
        is ContentBlockedException -> AiError.SafetyBlocked(this)

        is QuotaExceededException ->
            AiError.RateLimit(this)

        is RequestTimeoutException ->
            AiError.Timeout(this)

        is InvalidAPIKeyException,
        is APINotConfiguredException,
        is InvalidLocationException,
        is PermissionMissingException,
        is ServiceDisabledException ->
            AiError.Configuration(this)

        is ServerException ->
            AiError.Server(this)

        is ServiceConnectionHandshakeFailedException,
        is IOException ->
            AiError.Network(this)

        is ResponseStoppedException ->
            AiError.Unknown(this)

        else ->
            AiError.Unknown(this)
    }
}
