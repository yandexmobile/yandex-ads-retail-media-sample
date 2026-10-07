package com.yandex.retailmedia.sample.app

import com.yandex.retailmedia.sample.dto.ErrorResponse
import com.yandex.retailmedia.sample.error.ApiException
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException as MalformedBodyException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(cause.status, ErrorResponse(cause.code, cause.message))
        }
        exception<MalformedBodyException> { call, _ ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("bad_request", "Malformed request body"),
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("internal_error", cause.message ?: "Unexpected error"),
            )
        }
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", "Resource not found"))
        }
    }
}
