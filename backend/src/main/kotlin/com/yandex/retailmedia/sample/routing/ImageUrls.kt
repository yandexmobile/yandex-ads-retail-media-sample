package com.yandex.retailmedia.sample.routing

import com.yandex.retailmedia.sample.dto.CatalogResponse
import com.yandex.retailmedia.sample.model.Cart
import com.yandex.retailmedia.sample.model.Page
import com.yandex.retailmedia.sample.model.Product
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.host
import io.ktor.server.request.port

fun ApplicationCall.imageBaseUrl(): String = "${request.origin.scheme}://${request.host()}:${request.port()}"

fun Product.withImageUrl(baseUrl: String): Product = copy(picture = baseUrl + picture)

fun Page<Product>.withImageUrls(baseUrl: String): Page<Product> =
    copy(items = items.map { it.withImageUrl(baseUrl) })

fun Cart.withImageUrls(baseUrl: String): Cart =
    copy(items = items.map { it.copy(picture = baseUrl + it.picture) })

fun CatalogResponse.withImageUrls(baseUrl: String): CatalogResponse =
    copy(products = products.copy(items = products.items.map { it.withImageUrl(baseUrl) }))
