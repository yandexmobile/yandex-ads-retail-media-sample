package com.yandex.retailmedia.sample.cart

import com.yandex.retailmedia.sample.catalog.CatalogService
import com.yandex.retailmedia.sample.dto.CheckoutResult
import com.yandex.retailmedia.sample.error.BadRequestException
import com.yandex.retailmedia.sample.error.NotFoundException
import com.yandex.retailmedia.sample.model.Cart
import com.yandex.retailmedia.sample.model.CartItem

class CartService(private val catalog: CatalogService) {

    private val lock = Any()
    private val quantities = LinkedHashMap<String, Int>()

    fun get(): Cart = synchronized(lock) { snapshot() }

    fun add(productId: String, quantity: Int): Cart = synchronized(lock) {
        if (quantity < 1) throw BadRequestException("quantity must be >= 1")
        if (catalog.productById(productId) == null) {
            throw NotFoundException("product '$productId' not found")
        }
        quantities[productId] = (quantities[productId] ?: 0) + quantity
        snapshot()
    }

    fun remove(productId: String): Cart = synchronized(lock) {
        if (quantities.remove(productId) == null) {
            throw NotFoundException("product '$productId' not in cart")
        }
        snapshot()
    }

    fun checkout(): CheckoutResult = synchronized(lock) {
        if (quantities.isEmpty()) throw BadRequestException("cart is empty")
        quantities.clear()
        CheckoutResult(ok = true, message = "Order placed")
    }

    private fun snapshot(): Cart {
        val items = quantities.mapNotNull { (id, qty) ->
            catalog.productById(id)?.let { p ->
                CartItem(id = p.id, name = p.name, price = p.price, picture = p.picture, quantity = qty)
            }
        }
        return Cart(items = items, total = items.sumOf { it.price * it.quantity })
    }
}
