package com.yandex.retailmedia.sample.dto

import com.yandex.retailmedia.sample.retailmedia.ClientAdContext
import com.yandex.retailmedia.sample.retailmedia.Platform
import com.yandex.retailmedia.sample.retailmedia.Screen
import kotlinx.serialization.Serializable

interface AdContextRequest {
    val platform: Platform
    val bidderToken: String?
    val adSessionId: String?
    val adSessionHitNumber: Int?

    fun adContext(): ClientAdContext =
        ClientAdContext(
            platform = platform,
            bidderToken = bidderToken,
            adSessionId = adSessionId,
            adSessionHitNumber = adSessionHitNumber,
        )
}

@Serializable
data class CatalogAdRequest(
    val page: Int = 1,
    val pageSize: Int = 20,
    override val platform: Platform,
    override val bidderToken: String? = null,
    override val adSessionId: String? = null,
    override val adSessionHitNumber: Int? = null,
) : AdContextRequest

@Serializable
data class SearchAdRequest(
    val text: String,
    val page: Int = 1,
    val pageSize: Int = 20,
    override val platform: Platform,
    override val bidderToken: String? = null,
    override val adSessionId: String? = null,
    override val adSessionHitNumber: Int? = null,
) : AdContextRequest

@Serializable
data class AdsRequest(
    val screen: Screen,
    override val platform: Platform,
    override val bidderToken: String? = null,
    override val adSessionId: String? = null,
    override val adSessionHitNumber: Int? = null,
) : AdContextRequest
