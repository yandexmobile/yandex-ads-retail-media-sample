package com.yandex.retailmedia.sample.data

import com.yandex.retailmedia.sample.data.api.StoreApi
import com.yandex.retailmedia.sample.data.dto.AddToCartRequestDto
import com.yandex.retailmedia.sample.data.mapper.toDomain
import com.yandex.retailmedia.sample.domain.model.Cart
import com.yandex.retailmedia.sample.domain.model.CheckoutResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val api: StoreApi,
) : CartRepository {

    private val emptyCart = Cart(items = emptyList())
    private val _cart = MutableStateFlow(emptyCart)
    override val cart: StateFlow<Cart> = _cart.asStateFlow()

    override suspend fun get(): Cart = api.cart().toDomain().also { _cart.value = it }

    override suspend fun add(productId: String, quantity: Int): Cart =
        api.addToCart(AddToCartRequestDto(productId = productId, quantity = quantity)).toDomain()
            .also { _cart.value = it }

    override suspend fun remove(productId: String): Cart =
        api.removeFromCart(productId).toDomain().also { _cart.value = it }

    override suspend fun setQuantity(productId: String, quantity: Int): Cart {
        val afterRemove = api.removeFromCart(productId).toDomain()
        val result = if (quantity > 0) {
            try {
                api.addToCart(AddToCartRequestDto(productId = productId, quantity = quantity)).toDomain()
            } catch (e: Exception) {
                _cart.value = afterRemove
                throw e
            }
        } else {
            afterRemove
        }
        _cart.value = result
        return result
    }

    override suspend fun checkout(): CheckoutResult {
        val result = api.checkout().toDomain()
        if (result.ok) _cart.value = emptyCart
        return result
    }
}
