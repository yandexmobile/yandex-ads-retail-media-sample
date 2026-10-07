package com.yandex.retailmedia.sample.presentation.cart

import com.yandex.retailmedia.sample.domain.model.CartItem

sealed interface CartUiState {
    object Loading : CartUiState
    object Empty : CartUiState
    object Error : CartUiState
    data class Content(val items: List<CartItem>, val total: Double) : CartUiState
}

enum class CartEvent { CheckedOut, ActionFailed }
