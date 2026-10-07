package com.yandex.retailmedia.sample.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CatalogResponseDto(
    val products: PageDto<ProductDto>,
    val adSlot: AdSlotDto? = null,
)
