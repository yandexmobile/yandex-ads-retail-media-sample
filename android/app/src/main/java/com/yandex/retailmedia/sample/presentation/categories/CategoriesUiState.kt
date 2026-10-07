package com.yandex.retailmedia.sample.presentation.categories

import com.yandex.retailmedia.sample.domain.model.Category

sealed interface CategoriesUiState {
    object Loading : CategoriesUiState
    object Empty : CategoriesUiState
    object Error : CategoriesUiState
    data class Content(val items: List<Category>) : CategoriesUiState
}
