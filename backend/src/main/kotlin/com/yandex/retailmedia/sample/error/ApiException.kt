package com.yandex.retailmedia.sample.error

import io.ktor.http.HttpStatusCode

sealed class ApiException(
    val code: String,
    override val message: String,
    val status: HttpStatusCode,
) : RuntimeException(message)

class NotFoundException(message: String) :
    ApiException(code = "not_found", message = message, status = HttpStatusCode.NotFound)

class BadRequestException(message: String) :
    ApiException(code = "bad_request", message = message, status = HttpStatusCode.BadRequest)
