package com.yandex.retailmedia.sample.data

import com.yandex.retailmedia.sample.domain.model.Cart
import com.yandex.retailmedia.sample.domain.model.CheckoutResult
import kotlinx.coroutines.flow.StateFlow

interface CartRepository {

    val cart: StateFlow<Cart>

    suspend fun get(): Cart

    suspend fun add(productId: String, quantity: Int): Cart

    suspend fun remove(productId: String): Cart

    suspend fun setQuantity(productId: String, quantity: Int): Cart

    suspend fun checkout(): CheckoutResult
}
