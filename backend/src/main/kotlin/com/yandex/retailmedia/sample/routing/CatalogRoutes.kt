package com.yandex.retailmedia.sample.routing

import com.yandex.retailmedia.sample.catalog.CatalogService
import com.yandex.retailmedia.sample.dto.CatalogAdRequest
import com.yandex.retailmedia.sample.dto.SearchAdRequest
import com.yandex.retailmedia.sample.error.BadRequestException
import com.yandex.retailmedia.sample.retailmedia.ProductPromoService
import com.yandex.retailmedia.sample.retailmedia.Screen
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.catalogRoutes(catalog: CatalogService, productPromo: ProductPromoService) {
    post("/home") {
        val body = call.receive<CatalogAdRequest>()
        val page = catalog.home(body.page, body.pageSize)
        val response = productPromo.merge(
            basePage = page,
            screen = Screen.HOME,
            context = body.adContext(),
            text = null,
            categoryId = null,
            keepOrganicFirst = false,
        )
        call.respond(response.withImageUrls(call.imageBaseUrl()))
    }
    get("/categories") {
        call.respond(catalog.categories())
    }
    post("/categories/{id}/products") {
        val id = call.parameters["id"] ?: throw BadRequestException("category id required")
        val body = call.receive<CatalogAdRequest>()
        val page = catalog.productsInCategory(id, body.page, body.pageSize)
        val response = productPromo.merge(
            basePage = page,
            screen = Screen.CATEGORY,
            context = body.adContext(),
            text = null,
            categoryId = id,
            keepOrganicFirst = false,
        )
        call.respond(response.withImageUrls(call.imageBaseUrl()))
    }
    post("/search") {
        val body = call.receive<SearchAdRequest>()
        val page = catalog.search(body.text, body.page, body.pageSize)
        val response = productPromo.merge(
            basePage = page,
            screen = Screen.SEARCH,
            context = body.adContext(),
            text = body.text,
            categoryId = null,
            keepOrganicFirst = true,
        )
        call.respond(response.withImageUrls(call.imageBaseUrl()))
    }
}
