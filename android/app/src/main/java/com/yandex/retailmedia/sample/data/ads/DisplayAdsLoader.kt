@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample.data.ads

import com.yandex.mobile.ads.retailmedia.RetailMediaAd
import javax.inject.Inject

// [Step 2] [Step 3]
/**
 * Display ads: asks the backend for the screen's ad slot and passes its `adUnitId` and
 * `readyResponse` to `RetailMediaLoader.loadAd`.
 *
 * Called on every screen refresh, not once when the view model is created: `/ads` runs an auction per
 * request, and the view model outlives the view, so a creative loaded once would stay for the whole
 * visit to the tab. Ads are best-effort: the screen works without them, so no failure leaks out.
 */
class DisplayAdsLoader @Inject constructor(
    private val adsRepository: AdsRepository,
    private val adRepo: RetailMediaAdRepository,
) {

    suspend fun load(screen: String): List<RetailMediaAd> =
        bestEffort { adsRepository.screenAd(screen) }
            ?.let { slot -> bestEffort { adRepo.loadDisplayAds(slot.adUnitId, slot.readyResponse) } }
            .orEmpty()
}
