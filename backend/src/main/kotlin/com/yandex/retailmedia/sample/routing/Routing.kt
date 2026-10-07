package com.yandex.retailmedia.sample.routing

import com.yandex.retailmedia.sample.cart.CartService
import com.yandex.retailmedia.sample.catalog.CatalogService
import com.yandex.retailmedia.sample.feed.FeedService
import com.yandex.retailmedia.sample.retailmedia.DisplayAdsService
import com.yandex.retailmedia.sample.retailmedia.ProductPromoService
import io.ktor.server.application.Application
import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.routing

fun Application.configureRouting(
    catalog: CatalogService,
    cart: CartService,
    productPromo: ProductPromoService,
    displayAds: DisplayAdsService,
    feed: FeedService,
) {
    routing {
        staticResources("/images", "products")
        healthRoutes()
        catalogRoutes(
            catalog = catalog,
            productPromo = productPromo,
        )
        adsRoutes(displayAds)
        cartRoutes(cart)
        feedRoutes(feed)
    }
}
