package com.yandex.retailmedia.sample.retailmedia

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RmS2sRequest(
    @SerialName("imp-id") val impId: String,
    val text: String? = null,
    @SerialName("bidder-token") val bidderToken: String,
    @SerialName("category-id") val categoryId: String? = null,
    val s2s: String = "true",
    @SerialName("ad-session-id") val adSessionId: String? = null,
    @SerialName("ad-session-hit-number") val adSessionHitNumber: Int? = null,
    @SerialName("retail-feed-id") val retailFeedId: String,
)

@Serializable
data class RmS2sResponse(
    val status: String? = null,
    val items: List<RmItem> = emptyList(),
    val errors: List<String> = emptyList(),
) {
    val ok: Boolean get() = status == null || status.equals("OK", ignoreCase = true)
}

@Serializable
data class RmItem(val result: List<RmPlacement> = emptyList())

@Serializable
data class RmPlacement(
    val position: Int = 0,
    val format: String? = null,
    @SerialName("ready_response") val readyResponse: String? = null,
    val creatives: List<RmCreative> = emptyList(),
)

@Serializable
data class RmCreative(
    val urls: RmUrls? = null,
    @SerialName("client_info") val clientInfo: RmClientInfo? = null,
)

@Serializable
data class RmUrls(val service: RmService? = null)

@Serializable
data class RmService(@SerialName("out_of_stock") val outOfStock: String? = null)

@Serializable
data class RmClientInfo(@SerialName("retailer_info") val retailerInfo: RmRetailerInfo? = null)

@Serializable
data class RmRetailerInfo(@SerialName("offer_id") val offerId: String? = null)

fun RmS2sResponse.toRmAds(): RmAds {
    val slots = items.flatMap { it.result }
    val creatives = slots.flatMap { slot ->
        slot.creatives.map { creative ->
            RmCreativeInfo(
                offerId = creative.clientInfo?.retailerInfo?.offerId,
                position = slot.position,
                outOfStockUrl = creative.urls?.service?.outOfStock,
            )
        }
    }
    return RmAds(readyResponse = slots.firstNotNullOfOrNull { it.readyResponse }, creatives = creatives)
}
