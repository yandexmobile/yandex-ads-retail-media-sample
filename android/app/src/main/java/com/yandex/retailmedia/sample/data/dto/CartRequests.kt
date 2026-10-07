package com.yandex.retailmedia.sample.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddToCartRequestDto(val productId: String, val quantity: Int)

@Serializable
data class CheckoutResultDto(val ok: Boolean, val message: String)
