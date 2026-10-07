package com.yandex.retailmedia.sample.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class PageDto<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val hasMore: Boolean,
)
