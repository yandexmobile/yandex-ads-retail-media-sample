@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample.presentation.common

import com.yandex.mobile.ads.retailmedia.RetailMediaAd
import com.yandex.retailmedia.sample.domain.model.Product

class ProductRow(
    val product: Product,
    val quantityInCart: Int,
    val ad: RetailMediaAd? = null,
) : FeedItem

internal fun buildProductRows(
    products: List<Product>,
    quantities: Map<String, Int>,
    productPromoAds: List<RetailMediaAd>,
): List<ProductRow> {
    val adsByOfferId = productPromoAds.byOfferId()
    return products.map { product ->
        val ad = if (product.sponsored) adsByOfferId[product.id] else null
        ProductRow(product, quantities[product.id] ?: 0, ad)
    }
}

private fun List<RetailMediaAd>.byOfferId(): Map<String, RetailMediaAd> = buildMap {
    for (ad in this@byOfferId) {
        val offerId = ad.adInfo.creatives.firstNotNullOfOrNull { it.offerId } ?: continue
        putIfAbsent(offerId, ad)
    }
}
