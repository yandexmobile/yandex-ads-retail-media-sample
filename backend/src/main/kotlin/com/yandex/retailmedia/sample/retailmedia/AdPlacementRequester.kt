package com.yandex.retailmedia.sample.retailmedia

import com.yandex.retailmedia.sample.model.AdSlot
import kotlinx.coroutines.CancellationException
import org.slf4j.LoggerFactory

/**
 * Picks the ad unit for "platform + screen + format" and asks RM for it. Ads are best-effort: with
 * no bidder token, no configured ad unit or a failed RM call the store answers without ads.
 */
class AdPlacementRequester(
    private val client: RetailMediaClient,
    private val config: RetailMediaConfig,
) {

    suspend fun request(
        key: PlacementKey,
        context: ClientAdContext,
        text: String?,
        categoryId: String?,
    ): PlacedAds? {
        val placement = config.placement(key)
        val bidderToken = context.bidderToken
        if (bidderToken == null || placement == null) {
            logSkipped(key = key, hasBidderToken = bidderToken != null)
            return null
        }
        val requestContext = AdRequestContext(
            pageId = placement.pageId,
            impId = placement.impId,
            text = text,
            categoryId = categoryId,
            bidderToken = bidderToken,
            adSessionId = context.adSessionId,
            adSessionHitNumber = context.adSessionHitNumber,
        )
        val ads = requestBestEffort(requestContext) ?: return null
        return PlacedAds(
            adUnitId = placement.adUnitId,
            readyResponse = ads.readyResponse,
            creatives = ads.creatives,
        )
    }

    private suspend fun requestBestEffort(context: AdRequestContext): RmAds? =
        try {
            client.requestAds(context)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            logger.warn("RetailMedia request for page {} failed; serving without ads", context.pageId, failure)
            null
        }

    private fun logSkipped(key: PlacementKey, hasBidderToken: Boolean) {
        if (!logger.isDebugEnabled) return
        val reason = if (hasBidderToken) {
            "no ${key.format} placement is configured for platform '${key.platform.key}'"
        } else {
            "the client sent no bidder token"
        }
        logger.debug("{}: skipping RM, {}", key.screen.key, reason)
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(AdPlacementRequester::class.java)
    }
}

class PlacedAds(
    val adUnitId: String,
    val readyResponse: String?,
    val creatives: List<RmCreativeInfo>,
) {
    val adSlot: AdSlot?
        get() = readyResponse?.let { AdSlot(adUnitId = adUnitId, readyResponse = it) }
}
