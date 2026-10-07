package com.yandex.retailmedia.sample.retailmedia

import com.yandex.retailmedia.sample.catalog.CatalogService
import com.yandex.retailmedia.sample.dto.CatalogResponse
import com.yandex.retailmedia.sample.model.AdSlot
import com.yandex.retailmedia.sample.model.Page
import com.yandex.retailmedia.sample.model.Product
import org.slf4j.LoggerFactory

class ProductPromoService(
    private val catalog: CatalogService,
    private val requester: AdPlacementRequester,
    private val outOfStock: OutOfStockReporter,
) {

    // [Step 2.2]
    /**
     * productPromo: calls RM s2s for the screen's ad unit, enriches each returned creative with the
     * catalog product by `offer_id`, marks it `sponsored` and attaches an [AdSlot] to the response —
     * the client passes it to `loadAd`.
     *
     * An offer missing from the catalog or out of stock is not shown: `out_of_stock` is called for it.
     * Ads are best-effort here — the shopper must get the catalog even when RM is unavailable, so no
     * RM failure leaks out.
     */
    suspend fun merge(
        basePage: Page<Product>,
        screen: Screen,
        context: ClientAdContext,
        text: String?,
        categoryId: String?,
        keepOrganicFirst: Boolean,
    ): CatalogResponse {
        val placed = requester.request(
            key = PlacementKey(
                platform = context.platform,
                screen = screen,
                format = AdFormat.PRODUCT_PROMO,
            ),
            context = context,
            text = text,
            categoryId = categoryId,
        ) ?: return CatalogResponse(products = basePage, adSlot = null)

        val promoted = promotedProducts(screen = screen, creatives = placed.creatives)
        val items = if (keepOrganicFirst) {
            organicFirst(base = basePage.items, promoted = promoted)
        } else {
            atRmPositions(base = basePage.items, promoted = promoted)
        }
        val adSlot = placed.adSlot
        logger.debug(
            "{}: merged {} of {} sponsored products, adSlot={}",
            screen.key,
            promoted.size,
            placed.creatives.size,
            adSlot?.adUnitId ?: "none",
        )
        return CatalogResponse(products = basePage.copy(items = items), adSlot = adSlot)
    }

    private fun promotedProducts(screen: Screen, creatives: List<RmCreativeInfo>): Map<String, PromotedProduct> {
        val outOfStockUrls = mutableListOf<String>()
        val promoted = mutableMapOf<String, PromotedProduct>()
        for (creative in creatives) {
            val product = creative.offerId?.let { catalog.productById(it) }
            if (product == null || !product.available) {
                logger.debug(
                    "{}: offer {} is not in the catalog or is unavailable, reporting out of stock",
                    screen.key,
                    creative.offerId,
                )
                creative.outOfStockUrl?.let { outOfStockUrls += it }
                continue
            }
            promoted.putIfAbsent(
                product.id,
                PromotedProduct(
                    product = product.copy(sponsored = true),
                    position = creative.position,
                ),
            )
        }
        outOfStock.report(outOfStockUrls)
        return promoted
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(ProductPromoService::class.java)
    }
}
