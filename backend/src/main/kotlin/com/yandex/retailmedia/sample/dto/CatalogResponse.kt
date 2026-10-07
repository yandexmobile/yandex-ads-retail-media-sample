package com.yandex.retailmedia.sample.dto

import com.yandex.retailmedia.sample.model.AdSlot
import com.yandex.retailmedia.sample.model.Page
import com.yandex.retailmedia.sample.model.Product
import kotlinx.serialization.Serializable

/** Catalog response with RM: the paged products (some `sponsored`) plus an optional [AdSlot]. */
@Serializable
data class CatalogResponse(
    val products: Page<Product>,
    val adSlot: AdSlot? = null,
)
