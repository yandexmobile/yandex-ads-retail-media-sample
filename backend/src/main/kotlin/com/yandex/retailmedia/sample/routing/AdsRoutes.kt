package com.yandex.retailmedia.sample.routing

import com.yandex.retailmedia.sample.dto.AdsRequest
import com.yandex.retailmedia.sample.error.BadRequestException
import com.yandex.retailmedia.sample.retailmedia.AdFormat
import com.yandex.retailmedia.sample.retailmedia.DisplayAdsService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.adsRoutes(displayAds: DisplayAdsService) {
    post("/ads") {
        val body = call.receive<AdsRequest>()
        if (AdFormat.DISPLAY !in body.screen.formats) {
            throw BadRequestException("screen '${body.screen.key}' has no display ad")
        }
        val slot = displayAds.load(
            screen = body.screen,
            context = body.adContext(),
        )
        if (slot == null) call.respond(HttpStatusCode.NoContent) else call.respond(slot)
    }
}
