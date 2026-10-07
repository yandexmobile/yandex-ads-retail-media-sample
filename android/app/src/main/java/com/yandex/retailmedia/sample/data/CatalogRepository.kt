package com.yandex.retailmedia.sample.data

import com.yandex.retailmedia.sample.domain.model.CatalogPage
import com.yandex.retailmedia.sample.domain.model.Category

interface CatalogRepository {

    suspend fun home(page: Int, pageSize: Int): CatalogPage

    suspend fun categories(): List<Category>

    suspend fun productsInCategory(categoryId: String, page: Int, pageSize: Int): CatalogPage

    suspend fun search(text: String, page: Int, pageSize: Int): CatalogPage
}
