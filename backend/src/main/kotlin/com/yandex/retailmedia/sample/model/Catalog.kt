package com.yandex.retailmedia.sample.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(val id: String, val name: String)

/** [id] is the catalog key and the RM `offer_id` — the join key between the catalog and RetailMedia ads; [sponsored] marks a productPromo card. */
@Serializable
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val oldPrice: Double? = null,
    val currencyId: String,
    val picture: String,
    val categoryId: String,
    val url: String,
    val available: Boolean,
    val vendor: String? = null,
    val sponsored: Boolean = false,
)

@Serializable
data class Page<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val hasMore: Boolean,
)
