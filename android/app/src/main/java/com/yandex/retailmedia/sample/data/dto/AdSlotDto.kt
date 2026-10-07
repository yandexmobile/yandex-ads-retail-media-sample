package com.yandex.retailmedia.sample.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AdSlotDto(
    val adUnitId: String,
    val readyResponse: String,
)
