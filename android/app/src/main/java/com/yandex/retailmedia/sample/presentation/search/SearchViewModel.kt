package com.yandex.retailmedia.sample.presentation.search

import com.yandex.retailmedia.sample.data.CartRepository
import com.yandex.retailmedia.sample.data.CatalogRepository
import com.yandex.retailmedia.sample.data.ads.RetailMediaAdRepository
import com.yandex.retailmedia.sample.domain.model.CatalogPage
import com.yandex.retailmedia.sample.presentation.common.PagedProductsViewModel
import com.yandex.retailmedia.sample.presentation.common.ProductsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    cart: CartRepository,
    adRepo: RetailMediaAdRepository,
) : PagedProductsViewModel(cart, adRepo, initialState = ProductsUiState.Idle) {

    private var query: String = ""

    val hasQuery: Boolean get() = query.isNotEmpty()

    fun search(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        query = trimmed
        refresh()
    }

    override suspend fun fetch(page: Int, pageSize: Int): CatalogPage =
        catalog.search(query, page, pageSize)
}
