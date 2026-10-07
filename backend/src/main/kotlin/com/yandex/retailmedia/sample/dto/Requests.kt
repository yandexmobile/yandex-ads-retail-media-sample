package com.yandex.retailmedia.sample.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddToCartRequest(val productId: String, val quantity: Int = 1)
