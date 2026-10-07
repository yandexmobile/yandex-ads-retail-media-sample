@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample.presentation.home

import androidx.lifecycle.viewModelScope
import com.yandex.mobile.ads.retailmedia.RetailMediaAd
import com.yandex.retailmedia.sample.data.CartRepository
import com.yandex.retailmedia.sample.data.CatalogRepository
import com.yandex.retailmedia.sample.data.ads.AdScreens
import com.yandex.retailmedia.sample.data.ads.DisplayAdsLoader
import com.yandex.retailmedia.sample.data.ads.RetailMediaAdRepository
import com.yandex.retailmedia.sample.domain.model.CatalogPage
import com.yandex.retailmedia.sample.presentation.common.AdSliderRow
import com.yandex.retailmedia.sample.presentation.common.FeedItem
import com.yandex.retailmedia.sample.presentation.common.PagedProductsViewModel
import com.yandex.retailmedia.sample.presentation.common.ProductRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The only screen with both RetailMedia formats at once, each from its own ad unit: sponsored
 * products come in the catalog response (handled by [PagedProductsViewModel]), while display ads are
 * requested separately via `/ads`, as on Categories and Cart.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    private val displayAdsLoader: DisplayAdsLoader,
    adRepo: RetailMediaAdRepository,
    cart: CartRepository,
) : PagedProductsViewModel(cart, adRepo) {

    private var displayAds: List<RetailMediaAd>? = null
    private var adsJob: Job? = null

    init {
        refresh()
    }

    override fun refresh() {
        super.refresh()
        reloadDisplayAds()
    }

    override fun feed(rows: List<ProductRow>): List<FeedItem> {
        val ads = displayAds.orEmpty()
        if (ads.isEmpty()) return rows
        return rows.take(AD_SLIDER_POSITION) + AdSliderRow(ads) + rows.drop(AD_SLIDER_POSITION)
    }

    override suspend fun fetch(page: Int, pageSize: Int): CatalogPage = catalog.home(page, pageSize)

    private fun reloadDisplayAds() {
        adsJob?.cancel()
        displayAds = emptyList()
        adsJob = viewModelScope.launch {
            displayAds = displayAdsLoader.load(AdScreens.HOME)
            rebuildFeed()
        }
    }

    private companion object {
        private const val AD_SLIDER_POSITION = 2
    }
}
