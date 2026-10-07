package com.yandex.retailmedia.sample.routing

import com.yandex.retailmedia.sample.cart.CartService
import com.yandex.retailmedia.sample.dto.AddToCartRequest
import com.yandex.retailmedia.sample.error.BadRequestException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.cartRoutes(cart: CartService) {
    get("/cart") {
        call.respond(cart.get().withImageUrls(call.imageBaseUrl()))
    }
    post("/cart/items") {
        val body = call.receive<AddToCartRequest>()
        call.respond(cart.add(body.productId, body.quantity).withImageUrls(call.imageBaseUrl()))
    }
    delete("/cart/items/{id}") {
        val id = call.parameters["id"] ?: throw BadRequestException("product id required")
        call.respond(cart.remove(id).withImageUrls(call.imageBaseUrl()))
    }
    post("/cart/checkout") {
        call.respond(cart.checkout())
    }
}
