package com.yandex.retailmedia.sample.retailmedia

import com.yandex.retailmedia.sample.model.AdSlot
import org.slf4j.LoggerFactory

class DisplayAdsService(
    private val requester: AdPlacementRequester,
) {

    // [Step 2.3]
    /**
     * Display ads (`/ads`): the same s2s call but without enrichment — `ready_response` is relayed to
     * the client as is, and the partner adds no product data to it.
     */
    suspend fun load(screen: Screen, context: ClientAdContext): AdSlot? {
        val placed = requester.request(
            key = PlacementKey(
                platform = context.platform,
                screen = screen,
                format = AdFormat.DISPLAY,
            ),
            context = context,
            text = null,
            categoryId = null,
        ) ?: return null
        val slot = placed.adSlot
        if (slot == null) {
            logger.debug("{}: RM answered without a ready response, no banner to show", screen.key)
        }
        return slot
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(DisplayAdsService::class.java)
    }
}
