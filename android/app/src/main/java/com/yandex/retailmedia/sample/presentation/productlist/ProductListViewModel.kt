package com.yandex.retailmedia.sample.presentation.productlist

import androidx.lifecycle.SavedStateHandle
import com.yandex.retailmedia.sample.data.CartRepository
import com.yandex.retailmedia.sample.data.CatalogRepository
import com.yandex.retailmedia.sample.data.ads.RetailMediaAdRepository
import com.yandex.retailmedia.sample.domain.model.CatalogPage
import com.yandex.retailmedia.sample.presentation.common.PagedProductsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    cart: CartRepository,
    adRepo: RetailMediaAdRepository,
    savedStateHandle: SavedStateHandle,
) : PagedProductsViewModel(cart, adRepo) {

    private val categoryId: String =
        checkNotNull(savedStateHandle.get<String>("categoryId")) { "categoryId argument is required" }
    val categoryName: String = savedStateHandle.get<String>("categoryName").orEmpty()

    init {
        refresh()
    }

    override suspend fun fetch(page: Int, pageSize: Int): CatalogPage =
        catalog.productsInCategory(categoryId, page, pageSize)
}
