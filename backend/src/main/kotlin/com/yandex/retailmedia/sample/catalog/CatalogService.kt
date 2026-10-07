package com.yandex.retailmedia.sample.catalog

import com.yandex.retailmedia.sample.error.BadRequestException
import com.yandex.retailmedia.sample.error.NotFoundException
import com.yandex.retailmedia.sample.model.Category
import com.yandex.retailmedia.sample.model.Page
import com.yandex.retailmedia.sample.model.Product

class CatalogService(private val data: CatalogData = CatalogData) {

    fun categories(): List<Category> = data.categories

    fun allProducts(): List<Product> = data.products

    fun home(page: Int, pageSize: Int): Page<Product> = paginate(data.products, page, pageSize)

    fun productsInCategory(categoryId: String, page: Int, pageSize: Int): Page<Product> {
        if (data.categories.none { it.id == categoryId }) {
            throw NotFoundException("category '$categoryId' not found")
        }
        return paginate(data.products.filter { it.categoryId == categoryId }, page, pageSize)
    }

    fun search(text: String, page: Int, pageSize: Int): Page<Product> {
        if (text.isBlank()) throw BadRequestException("search text must not be blank")
        val query = text.trim()
        val matches = data.products
            .filter { it.name.contains(query, ignoreCase = true) }
            .sortedBy { rank(it.name, query) }
        return paginate(matches, page, pageSize)
    }

    private fun rank(name: String, query: String): Int = when {
        name.equals(query, ignoreCase = true) -> 0
        name.startsWith(query, ignoreCase = true) -> 1
        name.split(WORD_SEPARATOR).any { it.startsWith(query, ignoreCase = true) } -> 2
        else -> 3
    }

    fun productById(id: String): Product? = data.products.firstOrNull { it.id == id }

    private companion object {
        private val WORD_SEPARATOR = Regex("[\\s/-]+")
    }
}
