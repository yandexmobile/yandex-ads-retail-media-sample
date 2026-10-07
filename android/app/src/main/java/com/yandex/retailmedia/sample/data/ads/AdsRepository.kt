package com.yandex.retailmedia.sample.data.ads

import com.yandex.retailmedia.sample.data.api.StoreApi
import com.yandex.retailmedia.sample.data.dto.AD_REQUEST_PLATFORM
import com.yandex.retailmedia.sample.data.dto.AdsRequestDto
import com.yandex.retailmedia.sample.data.mapper.toDomain
import com.yandex.retailmedia.sample.domain.model.AdSlot
import javax.inject.Inject

object AdScreens {
    const val HOME = "home"
    const val CATEGORIES = "categories"
    const val CART = "cart"
}

// [Step 2]
/**
 * Requests display ads from our own backend. The body carries the SDK `bidderToken` and the ad
 * context — platform, screen, `adSessionId` and the hit number within the session; the backend uses
 * them to find the ad unit and call RM s2s. 204 means "no ads", which is a normal answer, not an error.
 */
class AdsRepository @Inject constructor(
    private val api: StoreApi,
    private val bidderTokenProvider: RetailMediaBidderTokenProvider,
    private val session: AdSessionTracker,
) {

    suspend fun screenAd(screen: String): AdSlot? {
        val response = api.ads(
            AdsRequestDto(
                screen = screen,
                platform = AD_REQUEST_PLATFORM,
                bidderToken = bidderTokenProvider.bidderToken(),
                adSessionId = session.sessionId,
                adSessionHitNumber = session.nextHitNumber(),
            ),
        )
        if (response.code() == 204) return null
        return response.body()?.toDomain()
    }
}
