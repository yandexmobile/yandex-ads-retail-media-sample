package com.yandex.retailmedia.sample.retailmedia

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Notifies RM about offers the store can't show. Sent in the background and in parallel: each
 * dropped creative needs its own request, and waiting on them one by one would put a chain of RM
 * round trips in front of the catalog.
 */
class OutOfStockReporter(
    private val client: RetailMediaClient,
    private val backgroundScope: CoroutineScope,
) {

    fun report(urls: List<String>) {
        if (urls.isEmpty()) return
        backgroundScope.launch {
            urls.forEach { url -> launch { client.reportOutOfStock(url) } }
        }
    }
}
