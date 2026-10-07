package com.yandex.retailmedia.sample.app

import com.yandex.retailmedia.sample.cart.CartService
import com.yandex.retailmedia.sample.catalog.CatalogService
import com.yandex.retailmedia.sample.feed.FeedService
import com.yandex.retailmedia.sample.retailmedia.AdPlacementRequester
import com.yandex.retailmedia.sample.retailmedia.DisplayAdsService
import com.yandex.retailmedia.sample.retailmedia.OutOfStockReporter
import com.yandex.retailmedia.sample.retailmedia.ProductPromoService
import com.yandex.retailmedia.sample.retailmedia.RealRetailMediaClient
import com.yandex.retailmedia.sample.retailmedia.RetailMediaClient
import com.yandex.retailmedia.sample.retailmedia.RetailMediaConfig
import com.yandex.retailmedia.sample.routing.configureRouting
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import kotlinx.coroutines.CoroutineScope

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val config = RetailMediaConfig.from(environment.config)
    storeModule(RealRetailMediaClient(config), config)
}

fun Application.storeModule(
    retailMedia: RetailMediaClient,
    rmConfig: RetailMediaConfig,
    backgroundScope: CoroutineScope = this,
) {
    configureSerialization()
    configureStatusPages()

    val catalog = CatalogService()
    val cart = CartService(catalog)
    val requester = AdPlacementRequester(
        client = retailMedia,
        config = rmConfig,
    )
    val productPromo = ProductPromoService(
        catalog = catalog,
        requester = requester,
        outOfStock = OutOfStockReporter(
            client = retailMedia,
            backgroundScope = backgroundScope,
        ),
    )
    val displayAds = DisplayAdsService(requester)
    val feed = FeedService(catalog)
    configureRouting(
        catalog = catalog,
        cart = cart,
        productPromo = productPromo,
        displayAds = displayAds,
        feed = feed,
    )
}
