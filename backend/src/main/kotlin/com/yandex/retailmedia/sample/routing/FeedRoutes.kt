package com.yandex.retailmedia.sample.routing

import com.yandex.retailmedia.sample.feed.FeedService
import io.ktor.http.ContentType
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.feedRoutes(feed: FeedService) {
    get("/feed") {
        call.respondText(feed.buildYml(call.imageBaseUrl()), ContentType.Application.Xml)
    }
}
