package com.yandex.retailmedia.sample.domain.model

data class Page<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val hasMore: Boolean,
)
