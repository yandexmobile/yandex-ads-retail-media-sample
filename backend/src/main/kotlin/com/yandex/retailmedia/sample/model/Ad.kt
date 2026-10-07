package com.yandex.retailmedia.sample.model

import kotlinx.serialization.Serializable

@Serializable
data class AdSlot(
    val adUnitId: String,
    val readyResponse: String,
)
