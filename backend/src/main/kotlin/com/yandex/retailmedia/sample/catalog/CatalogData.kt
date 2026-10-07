package com.yandex.retailmedia.sample.catalog

import com.yandex.retailmedia.sample.model.Category
import com.yandex.retailmedia.sample.model.Product

object CatalogData {
    val categories: List<Category> = listOf(
        Category("1", "Electronics"),
        Category("2", "Home"),
        Category("3", "Sports"),
        Category("4", "Books"),
    )

    val products: List<Product> = listOf(
        product("101", "Wireless Headphones", 5990.0, 7990.0, "1", "SoundMax"),
        product("102", "Smartphone Stand", 990.0, null, "1", null),
        product("103", "USB-C Cable 2m", 490.0, null, "1", "Cabli"),
        product("104", "Bluetooth Speaker", 3490.0, 3990.0, "1", "SoundMax"),
        product("201", "Ceramic Mug", 590.0, null, "2", null),
        product("202", "Table Lamp", 2490.0, null, "2", "Lumia"),
        product("203", "Throw Blanket", 1890.0, null, "2", null, available = false),
        product("301", "Yoga Mat", 1590.0, null, "3", "FlexFit"),
        product("302", "Water Bottle 750ml", 790.0, null, "3", null),
        product("303", "Running Socks", 390.0, null, "3", "FlexFit"),
        product("401", "Kotlin in Action", 3200.0, null, "4", "Manning"),
        product("402", "Clean Architecture", 2800.0, 3100.0, "4", "Prentice Hall"),
    )

    private fun product(
        id: String,
        name: String,
        price: Double,
        oldPrice: Double?,
        categoryId: String,
        vendor: String?,
        available: Boolean = true,
    ) = Product(
        id = id,
        name = name,
        price = price,
        oldPrice = oldPrice,
        currencyId = "RUB",
        picture = "/images/$id.png",
        categoryId = categoryId,
        url = "https://example.com/p/$id",
        available = available,
        vendor = vendor,
    )
}
