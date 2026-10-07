package com.yandex.retailmedia.sample.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val code: String, val message: String)

@Serializable
data class ServiceStatus(val status: String = "ok")

@Serializable
data class CheckoutResult(val ok: Boolean = true, val message: String)
