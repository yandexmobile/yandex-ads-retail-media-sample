package com.yandex.retailmedia.sample.domain.model

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val oldPrice: Double?,
    val currencyId: String,
    val picture: String,
    val categoryId: String,
    val url: String,
    val available: Boolean,
    val vendor: String?,
    val sponsored: Boolean = false,
)
