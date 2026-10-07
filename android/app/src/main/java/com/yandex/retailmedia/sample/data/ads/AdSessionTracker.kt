package com.yandex.retailmedia.sample.data.ads

import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

/**
 * Client-owned ad session for RetailMedia. [sessionId] identifies a continuous browsing session
 * and [nextHitNumber] counts ad requests within it. Both are sent in POST bodies and forwarded
 * to Yandex RM via the backend.
 */
class AdSessionTracker(
    idGenerator: () -> String = { UUID.randomUUID().toString() },
) {
    val sessionId: String = idGenerator()
    private val hit = AtomicInteger(0)

    fun nextHitNumber(): Int = hit.incrementAndGet()
}
