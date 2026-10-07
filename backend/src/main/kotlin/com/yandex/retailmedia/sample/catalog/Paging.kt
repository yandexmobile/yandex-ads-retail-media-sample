package com.yandex.retailmedia.sample.catalog

import com.yandex.retailmedia.sample.error.BadRequestException
import com.yandex.retailmedia.sample.model.Page

const val MAX_PAGE_SIZE = 100

fun <T> paginate(all: List<T>, page: Int, pageSize: Int): Page<T> {
    if (page < 1) throw BadRequestException("page must be >= 1")
    if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
        throw BadRequestException("pageSize must be between 1 and $MAX_PAGE_SIZE")
    }
    val from = (page - 1) * pageSize
    val items = if (from >= all.size) emptyList() else all.subList(from, minOf(from + pageSize, all.size))
    return Page(items = items, page = page, pageSize = pageSize, hasMore = from + items.size < all.size)
}
