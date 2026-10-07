package com.yandex.retailmedia.sample.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
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
