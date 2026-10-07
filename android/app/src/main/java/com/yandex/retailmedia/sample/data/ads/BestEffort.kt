package com.yandex.retailmedia.sample.data.ads

import kotlinx.coroutines.CancellationException

internal suspend inline fun <T> bestEffort(block: suspend () -> T): T? =
    try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Exception) {
        null
    }
