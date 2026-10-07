package com.yandex.retailmedia.sample.model

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val id: String,
    val name: String,
    val price: Double,
    val picture: String,
    val quantity: Int,
)

@Serializable
data class Cart(val items: List<CartItem>, val total: Double)
