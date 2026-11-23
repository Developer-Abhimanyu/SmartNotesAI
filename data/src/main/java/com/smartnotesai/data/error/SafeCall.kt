package com.smartnotesai.data.error

import com.smartnotesai.core.common.error.AppError
import com.smartnotesai.core.common.result.AppResult
import java.io.IOException
import kotlinx.coroutines.CancellationException

suspend fun <T> safeCall(
    block: suspend () -> T
): AppResult<T> {

    return try {
        AppResult.Success(block())

    } catch (exception: CancellationException) {
        throw exception

    } catch (exception: IOException) {
        AppResult.Error(AppError.Network)

    } catch (exception: Exception) {
        AppResult.Error(AppError.Unknown)
    }
}
