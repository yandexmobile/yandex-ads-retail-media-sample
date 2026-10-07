package com.yandex.retailmedia.sample.presentation.common

sealed interface ProductsUiState {

    object Idle : ProductsUiState

    object Loading : ProductsUiState

    object Empty : ProductsUiState

    object Error : ProductsUiState

    data class Content(
        val items: List<FeedItem>,
        val appending: Boolean = false,
        val hasMore: Boolean = false,
    ) : ProductsUiState
}
