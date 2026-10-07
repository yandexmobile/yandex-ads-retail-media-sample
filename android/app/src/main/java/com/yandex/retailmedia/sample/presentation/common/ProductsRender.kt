package com.yandex.retailmedia.sample.presentation.common

import androidx.core.view.isVisible
import com.yandex.retailmedia.sample.databinding.ScreenProductsBinding

fun renderProductsScreen(
    binding: ScreenProductsBinding,
    state: ProductsUiState,
    adapter: ProductAdapter,
) {
    binding.progress.isVisible = state is ProductsUiState.Loading && !binding.swipe.isRefreshing
    binding.emptyView.isVisible = state is ProductsUiState.Empty
    binding.errorView.isVisible = state is ProductsUiState.Error

    adapter.submitList((state as? ProductsUiState.Content)?.items.orEmpty())

    if (state !is ProductsUiState.Loading) {
        binding.swipe.isRefreshing = false
    }
}
