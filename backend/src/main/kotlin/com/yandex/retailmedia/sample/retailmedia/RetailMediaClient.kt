package com.yandex.retailmedia.sample.retailmedia

/** Single s2s entry point to Yandex RM, used by both the merged and dedicated paths. */
interface RetailMediaClient {

    /** Requests ads for one context; returns null when RM is disabled/unconfigured. */
    suspend fun requestAds(context: AdRequestContext): RmAds?

    /** Fire-and-forget out-of-stock tracking hit; must never throw. */
    suspend fun reportOutOfStock(url: String)
}
