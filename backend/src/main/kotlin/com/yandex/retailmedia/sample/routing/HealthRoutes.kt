package com.yandex.retailmedia.sample.routing

import com.yandex.retailmedia.sample.dto.ServiceStatus
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.healthRoutes() {
    get("/") {
        call.respond(ServiceStatus())
    }
}
