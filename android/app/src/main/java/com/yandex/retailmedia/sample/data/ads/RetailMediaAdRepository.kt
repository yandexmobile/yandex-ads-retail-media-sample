@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample.data.ads

import android.content.Context
import com.yandex.mobile.ads.retailmedia.RetailMediaAd
import com.yandex.mobile.ads.retailmedia.RetailMediaLoadResult
import com.yandex.mobile.ads.retailmedia.RetailMediaLoader
import com.yandex.mobile.ads.retailmedia.type.RetailMediaAdType
import dagger.hilt.android.qualifiers.ApplicationContext

// [Step 3]
/**
 * `RetailMediaLoader.loadAd(adUnitId, readyResponse)` turns the response the backend got from RM into
 * SDK ads. One `readyResponse` yields one `RetailMedia`, whose `retailMediaAds` may hold several
 * `RetailMediaAd`s — these are what gets laid out on the screen.
 *
 * `adType` separates the formats: `PRODUCT_PROMO` is bound into a product card, everything else is a
 * display ad. A failed load yields an empty list: the store must work without ads.
 */
class RetailMediaAdRepository(
    @ApplicationContext private val context: Context,
) {

    /** [offerIds] is a filter, not an order: the SDK keeps only the ads for these offers. */
    suspend fun loadProductPromo(
        adUnitId: String,
        readyResponse: String,
        offerIds: List<String>,
    ): List<RetailMediaAd> {
        val result = RetailMediaLoader(context).loadAd(adUnitId, readyResponse, offerIds.ifEmpty { null })
        return when (result) {
            is RetailMediaLoadResult.Success -> result.ad.retailMediaAds.filter { it.adType == RetailMediaAdType.PRODUCT_PROMO }
            is RetailMediaLoadResult.Failure -> emptyList()
        }
    }

    suspend fun loadDisplayAds(adUnitId: String, readyResponse: String): List<RetailMediaAd> {
        val result = RetailMediaLoader(context).loadAd(adUnitId, readyResponse)
        return when (result) {
            is RetailMediaLoadResult.Success -> result.ad.retailMediaAds.filter { it.adType != RetailMediaAdType.PRODUCT_PROMO }
            is RetailMediaLoadResult.Failure -> emptyList()
        }
    }
}
