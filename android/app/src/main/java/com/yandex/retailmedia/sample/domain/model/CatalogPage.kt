package com.yandex.retailmedia.sample.domain.model

data class CatalogPage(
    val products: Page<Product>,
    val adSlot: AdSlot?,
)
