@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yandex.mobile.ads.retailmedia.RetailMediaAd
import com.yandex.retailmedia.sample.data.CatalogRepository
import com.yandex.retailmedia.sample.data.ads.AdScreens
import com.yandex.retailmedia.sample.data.ads.DisplayAdsLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    private val displayAds: DisplayAdsLoader,
) : ViewModel() {

    private val _state = MutableStateFlow<CategoriesUiState>(CategoriesUiState.Loading)
    val state = _state.asStateFlow()

    private val _bannerAd = MutableStateFlow<RetailMediaAd?>(null)
    val bannerAd = _bannerAd.asStateFlow()

    private var bannerJob: Job? = null

    init {
        load()
    }

    fun load() {
        _state.value = CategoriesUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                val categories = catalog.categories()
                if (categories.isEmpty()) {
                    CategoriesUiState.Empty
                } else {
                    CategoriesUiState.Content(categories)
                }
            } catch (t: Throwable) {
                CategoriesUiState.Error
            }
        }
        reloadBanner()
    }

    private fun reloadBanner() {
        bannerJob?.cancel()
        _bannerAd.value = null
        bannerJob = viewModelScope.launch {
            _bannerAd.value = displayAds.load(AdScreens.CATEGORIES).firstOrNull()
        }
    }
}
