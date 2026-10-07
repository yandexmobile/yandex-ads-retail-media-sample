package com.yandex.retailmedia.sample.domain.model

data class CartItem(
    val id: String,
    val name: String,
    val price: Double,
    val picture: String,
    val quantity: Int,
)

data class Cart(val items: List<CartItem>) {
    val total: Double get() = items.sumOf { it.price * it.quantity }
}

data class CheckoutResult(val ok: Boolean, val message: String)
