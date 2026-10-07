package com.yandex.retailmedia.sample.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CartItemDto(
    val id: String,
    val name: String,
    val price: Double,
    val picture: String,
    val quantity: Int,
)

@Serializable
data class CartDto(val items: List<CartItemDto>)
