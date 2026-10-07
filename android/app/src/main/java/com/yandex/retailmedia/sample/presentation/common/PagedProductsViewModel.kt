@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yandex.mobile.ads.retailmedia.RetailMediaAd
import com.yandex.retailmedia.sample.data.CartRepository
import com.yandex.retailmedia.sample.data.ads.RetailMediaAdRepository
import com.yandex.retailmedia.sample.data.ads.bestEffort
import com.yandex.retailmedia.sample.domain.model.CatalogPage
import com.yandex.retailmedia.sample.domain.model.Product
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ProductsEvent { CartActionFailed }

abstract class PagedProductsViewModel(
    private val cart: CartRepository,
    private val adRepo: RetailMediaAdRepository,
    initialState: ProductsUiState = ProductsUiState.Loading,
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProductsEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private val loaded = mutableListOf<Product>()
    private val loadedIds = mutableSetOf<String>()
    private var page = 0
    private var hasMore = true
    private var loading = false
    private var productPromoAds: List<RetailMediaAd> = emptyList()
    private var lastAdReadyResponse: String? = null

    init {
        viewModelScope.launch {
            runCatching { cart.get() }.onFailure { _events.emit(ProductsEvent.CartActionFailed) }
        }
        viewModelScope.launch {
            cart.cart.collect { rebuildFeed() }
        }
    }

    protected abstract suspend fun fetch(page: Int, pageSize: Int): CatalogPage

    protected open fun feed(rows: List<ProductRow>): List<FeedItem> = rows

    protected fun rebuildFeed() {
        val current = _state.value
        if (current is ProductsUiState.Content) {
            _state.value = current.copy(items = feed(rows()))
        }
    }

    open fun refresh() {
        page = 0
        hasMore = true
        loaded.clear()
        loadedIds.clear()
        _state.value = ProductsUiState.Loading
        loadNext(reset = true)
    }

    fun loadMore() {
        if (loading || !hasMore) return
        (_state.value as? ProductsUiState.Content)?.let { _state.value = it.copy(appending = true) }
        loadNext(reset = false)
    }

    private fun loadNext(reset: Boolean) {
        if (loading) return
        loading = true
        viewModelScope.launch {
            try {
                val result = fetch(page + 1, pageSize)
                page += 1
                hasMore = result.products.hasMore
                result.products.items.forEach { if (loadedIds.add(it.id)) loaded += it }
                val adSlot = result.adSlot
                if (adSlot != null && adSlot.readyResponse != lastAdReadyResponse) {
                    lastAdReadyResponse = adSlot.readyResponse
                    productPromoAds = bestEffort {
                        adRepo.loadProductPromo(
                            adSlot.adUnitId,
                            adSlot.readyResponse,
                            loaded.filter { it.sponsored }.map { it.id },
                        )
                    }.orEmpty()
                }
                _state.value = if (loaded.isEmpty()) {
                    ProductsUiState.Empty
                } else {
                    ProductsUiState.Content(feed(rows()), appending = false, hasMore = hasMore)
                }
            } catch (t: Throwable) {
                _state.value = if (reset || loaded.isEmpty()) {
                    ProductsUiState.Error
                } else {
                    ProductsUiState.Content(feed(rows()), appending = false, hasMore = hasMore)
                }
            } finally {
                loading = false
            }
        }
    }

    fun increase(product: Product) = cartOp { cart.add(product.id, quantity = 1) }

    fun decrease(product: Product) = cartOp {
        val quantity = cart.cart.value.items.firstOrNull { it.id == product.id }?.quantity ?: 0
        cart.setQuantity(product.id, quantity - 1)
    }

    private fun cartOp(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }.onFailure { _events.emit(ProductsEvent.CartActionFailed) }
        }
    }

    private fun rows(): List<ProductRow> = buildProductRows(
        products = loaded,
        quantities = cart.cart.value.items.associate { it.id to it.quantity },
        productPromoAds = productPromoAds,
    )

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
