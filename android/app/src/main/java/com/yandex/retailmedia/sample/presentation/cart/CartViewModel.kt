@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yandex.mobile.ads.retailmedia.RetailMediaAd
import com.yandex.retailmedia.sample.data.CartRepository
import com.yandex.retailmedia.sample.data.ads.AdScreens
import com.yandex.retailmedia.sample.data.ads.DisplayAdsLoader
import com.yandex.retailmedia.sample.domain.model.Cart
import com.yandex.retailmedia.sample.domain.model.CartItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cart: CartRepository,
    private val displayAds: DisplayAdsLoader,
) : ViewModel() {

    private val _state = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<CartEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private val _bannerAd = MutableStateFlow<RetailMediaAd?>(null)
    val bannerAd = _bannerAd.asStateFlow()

    private var busy = false
    private var loaded = false
    private var bannerJob: Job? = null

    init {
        viewModelScope.launch {
            cart.cart.collect { if (loaded) _state.value = toState(it) }
        }
        refresh()
    }

    fun refresh() {
        _state.value = CartUiState.Loading
        viewModelScope.launch {
            runCatching { cart.get() }
                .onSuccess { loaded = true; _state.value = toState(it) }
                .onFailure { _state.value = CartUiState.Error }
        }
        reloadBanner()
    }

    private fun reloadBanner() {
        bannerJob?.cancel()
        _bannerAd.value = null
        bannerJob = viewModelScope.launch {
            _bannerAd.value = displayAds.load(AdScreens.CART).firstOrNull()
        }
    }

    fun increment(item: CartItem) = mutate { cart.add(item.id, quantity = 1) }

    fun decrement(item: CartItem) = mutate {
        val quantity = cart.cart.value.items.firstOrNull { it.id == item.id }?.quantity ?: 0
        cart.setQuantity(item.id, quantity - 1)
    }

    fun remove(item: CartItem) = mutate { cart.remove(item.id) }

    fun checkout() {
        if (busy) return
        busy = true
        viewModelScope.launch {
            runCatching { cart.checkout() }
                .onSuccess { result ->
                    if (result.ok) {
                        _events.emit(CartEvent.CheckedOut)
                        _state.value = CartUiState.Empty
                    } else {
                        _events.emit(CartEvent.ActionFailed)
                    }
                }
                .onFailure { _events.emit(CartEvent.ActionFailed) }
            busy = false
        }
    }

    private fun mutate(block: suspend () -> Cart) {
        if (busy) return
        busy = true
        viewModelScope.launch {
            runCatching { block() }
                .onSuccess { _state.value = toState(it) }
                .onFailure { _events.emit(CartEvent.ActionFailed) }
            busy = false
        }
    }

    private fun toState(cart: Cart): CartUiState =
        if (cart.items.isEmpty()) CartUiState.Empty else CartUiState.Content(cart.items, cart.total)
}
