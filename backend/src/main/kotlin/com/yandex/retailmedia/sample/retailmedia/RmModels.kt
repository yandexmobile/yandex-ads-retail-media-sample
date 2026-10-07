package com.yandex.retailmedia.sample.retailmedia

class ClientAdContext(
    val platform: Platform,
    val bidderToken: String?,
    val adSessionId: String?,
    val adSessionHitNumber: Int?,
)

data class AdRequestContext(
    val pageId: String,
    val impId: String,
    val text: String?,
    val categoryId: String?,
    val bidderToken: String,
    val adSessionId: String?,
    val adSessionHitNumber: Int?,
)

data class RmCreativeInfo(
    val offerId: String?,
    val position: Int,
    val outOfStockUrl: String?,
)

data class RmAds(
    val readyResponse: String?,
    val creatives: List<RmCreativeInfo>,
)
